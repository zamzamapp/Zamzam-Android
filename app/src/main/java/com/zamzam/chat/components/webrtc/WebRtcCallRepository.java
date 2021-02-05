package com.zamzam.chat.components.webrtc;

import android.media.AudioManager;

import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.util.ServiceUtil;

class WebRtcCallRepository {

  private final AudioManager audioManager;

  WebRtcCallRepository() {
    this.audioManager = ServiceUtil.getAudioManager(ApplicationDependencies.getApplication());
  }

  WebRtcAudioOutput getAudioOutput() {
    if (audioManager.isBluetoothScoOn()) {
      return WebRtcAudioOutput.HEADSET;
    } else if (audioManager.isSpeakerphoneOn()) {
      return WebRtcAudioOutput.SPEAKER;
    } else {
      return WebRtcAudioOutput.HANDSET;
    }
  }
}
