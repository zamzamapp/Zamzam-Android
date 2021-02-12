package com.zamzam.chat.ringrtc;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.annimon.stream.Stream;

import java.util.List;
import java.util.LinkedList;

import org.signal.ringrtc.CameraControl;

import com.zamzam.chat.logging.Log;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Capturer;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.CapturerObserver;
import org.webrtc.EglBase;
import org.webrtc.SurfaceTextureHelper;

import static com.zamzam.chat.ringrtc.CameraState.Direction.BACK;
import static com.zamzam.chat.ringrtc.CameraState.Direction.FRONT;
import static com.zamzam.chat.ringrtc.CameraState.Direction.NONE;
import static com.zamzam.chat.ringrtc.CameraState.Direction.PENDING;

/**
 * Encapsulate the camera functionality needed for video calling.
 */
public class Camera implements CameraControl, CameraVideoCapturer.CameraSwitchHandler {

  private static final String TAG = Log.tag(Camera.class);

  @NonNull  private final Context               context;
  @Nullable private final CameraVideoCapturer   capturer;
  @NonNull  private final CameraEventListener   cameraEventListener;
  @NonNull  private final EglBase               eglBase;
            private final int                   cameraCount;
  @NonNull  private       CameraState.Direction activeDirection;
            private       boolean               enabled;

  public Camera(@NonNull Context             context,
                @NonNull CameraEventListener cameraEventListener,
                @NonNull EglBase             eglBase)
  {
    this.context                = context;
    this.cameraEventListener    = cameraEventListener;
    this.eglBase                = eglBase;
    CameraEnumerator enumerator = getCameraEnumerator(context);
    cameraCount                 = enumerator.getDeviceNames().length;

    CameraVideoCapturer capturerCandidate = createVideoCapturer(enumerator, FRONT);
    if (capturerCandidate != null) {
      activeDirection = FRONT;
    } else {
      capturerCandidate = createVideoCapturer(enumerator, BACK);
      if (capturerCandidate != null) {
        activeDirection = BACK;
      } else {
        activeDirection = NONE;
      }
    }
    capturer = capturerCandidate;
  }

  @Override
  public void initCapturer(@NonNull CapturerObserver observer) {
    if (capturer != null) {
      capturer.initialize(SurfaceTextureHelper.create("WebRTC-SurfaceTextureHelper", eglBase.getEglBaseContext()),
                          context,
                          observer);
    }
  }

  @Override
  public boolean hasCapturer() {
    return capturer != null;
  }

  @Override
  public void flip() {
    if (capturer == null || cameraCount < 2) {
      throw new AssertionError("Tried to flip the camera, but we only have " + cameraCount + " of them.");
    }
    activeDirection = PENDING;
    capturer.switchCamera(this);
  }

  @Override
  public void setEnabled(boolean enabled) {
    Log.i(TAG, "setEnabled(): " + enabled);

    this.enabled = enabled;

    if (capturer == null) {
      return;
    }

    try {
      if (enabled) {
        Log.i(TAG, "setEnabled(): starting capture");
        capturer.startCapture(1280, 720, 30);
      } else {
        Log.i(TAG, "setEnabled(): stopping capture");
        capturer.stopCapture();
      }
    } catch (InterruptedException e) {
      Log.w(TAG, "Got interrupted while trying to stop video capture", e);
    }
  }

  public void dispose() {
    if (capturer != null) {
      capturer.dispose();
    }
  }

  int getCount() {
    return cameraCount;
  }

  @NonNull CameraState.Direction getActiveDirection() {
    return enabled ? activeDirection : NONE;
  }

  @NonNull public CameraState getCameraState() {
    return new CameraState(getActiveDirection(), getCount());
  }

  @Nullable CameraVideoCapturer getCapturer() {
    return capturer;
  }

  private @Nullable CameraVideoCapturer createVideoCapturer(@NonNull CameraEnumerator enumerator,
                                                            @NonNull CameraState.Direction direction)
  {
    String[] deviceNames = enumerator.getDeviceNames();
    for (String deviceName : deviceNames) {
      if ((direction == FRONT && enumerator.isFrontFacing(deviceName)) ||
          (direction == BACK  && enumerator.isBackFacing(deviceName)))
        {
          return enumerator.createCapturer(deviceName, null);
        }
    }

    return null;
  }

  private @NonNull CameraEnumerator getCameraEnumerator(@NonNull Context context) {
    boolean camera2EnumeratorIsSupported = false;
    try {
      camera2EnumeratorIsSupported = Camera2Enumerator.isSupported(context);
    } catch (final Throwable throwable) {
      Log.w(TAG, "Camera2Enumator.isSupport() threw.", throwable);
    }

    Log.i(TAG, "Camera2 enumerator supported: " + camera2EnumeratorIsSupported);

    return camera2EnumeratorIsSupported ? new FilteredCamera2Enumerator(context)
                                        : new Camera1Enumerator(true);
  }

  @Override
  public void onCameraSwitchDone(boolean isFrontFacing) {
    activeDirection = isFrontFacing ? FRONT : BACK;
    cameraEventListener.onCameraSwitchCompleted(new CameraState(getActiveDirection(), getCount()));
  }

  @Override
  public void onCameraSwitchError(String errorMessage) {
    Log.e(TAG, "onCameraSwitchError: " + errorMessage);
    cameraEventListener.onCameraSwitchCompleted(new CameraState(getActiveDirection(), getCount()));
  }

  @TargetApi(21)
  private static class FilteredCamera2Enumerator extends Camera2Enumerator {

    private static final String TAG = Log.tag(Camera2Enumerator.class);

    @NonNull  private final Context       context;
    @Nullable private final CameraManager cameraManager;
    @Nullable private       String[]      deviceNames;

    FilteredCamera2Enumerator(@NonNull Context context) {
      super(context);

      this.context       = context;
      this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
      this.deviceNames   = null;
    }

    private static boolean isMonochrome(@NonNull String deviceName, @NonNull CameraManager cameraManager) {
      try {
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(deviceName);
        int[]                 capabilities    = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);

        if (capabilities != null) {
          for (int capability : capabilities) {
            if (capability == CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME) {
              return true;
            }
          }
        }
      } catch (CameraAccessException e) {
        return false;
      }

      return false;
    }

    private static boolean isLensFacing(@NonNull String deviceName, @NonNull CameraManager cameraManager, @NonNull Integer facing) {
      try {
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(deviceName);
        Integer               lensFacing      = characteristics.get(CameraCharacteristics.LENS_FACING);

        return facing.equals(lensFacing);
      } catch (CameraAccessException e) {
        return false;
      }
    }

    @Override
    public @NonNull String[] getDeviceNames() {
      if (deviceNames != null) {
        return deviceNames;
      }

      try {
        List<String> cameraList = new LinkedList<>();

        if (cameraManager != null) {
          List<String> devices = Stream.of(cameraManager.getCameraIdList())
                                       .filterNot(id -> isMonochrome(id, cameraManager))
                                       .toList();

          String frontCamera = Stream.of(devices)
                                     .filter(id -> isLensFacing(id, cameraManager, CameraMetadata.LENS_FACING_FRONT))
                                     .findFirst()
                                     .orElse(null);

          if (frontCamera != null) {
            cameraList.add(frontCamera);
          }

          String backCamera = Stream.of(devices)
                                    .filter(id -> isLensFacing(id, cameraManager, CameraMetadata.LENS_FACING_BACK))
                                    .findFirst()
                                    .orElse(null);

          if (backCamera != null) {
            cameraList.add(backCamera);
          }
        }

        this.deviceNames = cameraList.toArray(new String[0]);
      } catch (CameraAccessException e) {
        Log.e(TAG, "Camera access exception: " + e);
        this.deviceNames = new String[] {};
      }

      return deviceNames;
    }

    @Override
    public @NonNull CameraVideoCapturer createCapturer(@Nullable String deviceName,
                                                       @Nullable CameraVideoCapturer.CameraEventsHandler eventsHandler)
    {
      return new Camera2Capturer(context, deviceName, eventsHandler, new FilteredCamera2Enumerator(context));
    }
  }
}