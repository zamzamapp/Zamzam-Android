package com.zamzam.chat.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.zamzam.chat.push.SignalServiceNetworkAccess;

public final class CensorshipUtil {

  private CensorshipUtil() {}

  public static boolean isCensored(@NonNull Context context) {
    return new SignalServiceNetworkAccess(context).isCensored(context);
  }
}
