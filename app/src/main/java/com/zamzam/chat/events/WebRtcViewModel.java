package com.zamzam.chat.events;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zamzam.chat.components.webrtc.TextureViewRenderer;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.ringrtc.CameraState;

import org.whispersystems.libsignal.IdentityKey;

public class WebRtcViewModel {

  public enum State {
    // Normal states
    CALL_INCOMING,
    CALL_OUTGOING,
    CALL_CONNECTED,
    CALL_RINGING,
    CALL_BUSY,
    CALL_DISCONNECTED,
    CALL_NEEDS_PERMISSION,

    // Error states
    NETWORK_FAILURE,
    RECIPIENT_UNAVAILABLE,
    NO_SUCH_USER,
    UNTRUSTED_IDENTITY,

    // Multiring Hangup States
    CALL_ACCEPTED_ELSEWHERE,
    CALL_DECLINED_ELSEWHERE,
    CALL_ONGOING_ELSEWHERE
  }


  private final @NonNull  State       state;
  private final @NonNull  Recipient   recipient;
  private final @Nullable IdentityKey identityKey;

  private final boolean remoteVideoEnabled;

  private final boolean isBluetoothAvailable;
  private final boolean isMicrophoneEnabled;
  private final boolean isRemoteVideoOffer;

  private final CameraState         localCameraState;
  private final TextureViewRenderer localRenderer;
  private final TextureViewRenderer remoteRenderer;

  private final long callConnectedTime;

  public WebRtcViewModel(@NonNull State               state,
                         @NonNull Recipient           recipient,
                         @NonNull CameraState         localCameraState,
                         @NonNull TextureViewRenderer localRenderer,
                         @NonNull TextureViewRenderer remoteRenderer,
                                  boolean             remoteVideoEnabled,
                                  boolean             isBluetoothAvailable,
                                  boolean             isMicrophoneEnabled,
                                  boolean             isRemoteVideoOffer,
                                  long                callConnectedTime)
  {
    this(state,
         recipient,
         null,
         localCameraState,
         localRenderer,
         remoteRenderer,
         remoteVideoEnabled,
         isBluetoothAvailable,
         isMicrophoneEnabled,
         isRemoteVideoOffer,
         callConnectedTime);
  }

  public WebRtcViewModel(@NonNull  State               state,
                         @NonNull  Recipient           recipient,
                         @Nullable IdentityKey         identityKey,
                         @NonNull  CameraState         localCameraState,
                         @NonNull  TextureViewRenderer localRenderer,
                         @NonNull  TextureViewRenderer remoteRenderer,
                                   boolean             remoteVideoEnabled,
                                   boolean             isBluetoothAvailable,
                                   boolean             isMicrophoneEnabled,
                                   boolean             isRemoteVideoOffer,
                                   long                callConnectedTime)
  {
    this.state                = state;
    this.recipient            = recipient;
    this.localCameraState     = localCameraState;
    this.localRenderer        = localRenderer;
    this.remoteRenderer       = remoteRenderer;
    this.identityKey          = identityKey;
    this.remoteVideoEnabled   = remoteVideoEnabled;
    this.isBluetoothAvailable = isBluetoothAvailable;
    this.isMicrophoneEnabled  = isMicrophoneEnabled;
    this.isRemoteVideoOffer   = isRemoteVideoOffer;
    this.callConnectedTime    = callConnectedTime;
  }

  public @NonNull State getState() {
    return state;
  }

  public @NonNull Recipient getRecipient() {
    return recipient;
  }

  public @NonNull CameraState getLocalCameraState() {
    return localCameraState;
  }

  public @Nullable IdentityKey getIdentityKey() {
    return identityKey;
  }

  public boolean isRemoteVideoEnabled() {
    return remoteVideoEnabled;
  }

  public boolean isBluetoothAvailable() {
    return isBluetoothAvailable;
  }

  public boolean isMicrophoneEnabled() {
    return isMicrophoneEnabled;
  }

  public boolean isRemoteVideoOffer() {
    return isRemoteVideoOffer;
  }

  public TextureViewRenderer getLocalRenderer() {
    return localRenderer;
  }

  public TextureViewRenderer getRemoteRenderer() {
    return remoteRenderer;
  }

  public long getCallConnectedTime() {
    return callConnectedTime;
  }

  public @NonNull String toString() {
    return "[State: "               + state +
           ", recipient: "          + recipient.getId().serialize() +
           ", identity: "           + identityKey +
           ", remoteVideo: "        + remoteVideoEnabled +
           ", localVideo: "         + localCameraState.isEnabled() +
           ", isRemoteVideoOffer: " + isRemoteVideoOffer +
           ", callConnectedTime: "  + callConnectedTime +
           "]";
  }
}
