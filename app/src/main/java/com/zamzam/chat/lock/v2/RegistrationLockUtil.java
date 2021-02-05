package com.zamzam.chat.lock.v2;

import android.content.Context;

import androidx.annotation.NonNull;

import com.zamzam.chat.keyvalue.SignalStore;
import com.zamzam.chat.util.TextSecurePreferences;

public final class RegistrationLockUtil {

  private RegistrationLockUtil() {}

  public static boolean userHasRegistrationLock(@NonNull Context context) {
    return TextSecurePreferences.isV1RegistrationLockEnabled(context) || SignalStore.kbsValues().isV2RegistrationLockEnabled();
  }
}
