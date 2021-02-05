package com.zamzam.chat.util;

import androidx.annotation.StyleRes;

import com.zamzam.chat.R;

public class DynamicNoActionBarInviteTheme extends DynamicTheme {

  protected @StyleRes int getLightThemeStyle() {
    return R.style.Signal_Light_NoActionBar_Invite;
  }

  protected @StyleRes int getDarkThemeStyle() {
    return R.style.Signal_NoActionBar_Invite;
  }
}
