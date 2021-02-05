package com.zamzam.chat.util;

import androidx.annotation.StyleRes;

import com.zamzam.chat.R;

public class DynamicRegistrationTheme extends DynamicTheme {

  protected @StyleRes int getLightThemeStyle() {
    return R.style.TextSecure_LightRegistrationTheme;
  }

  protected @StyleRes int getDarkThemeStyle() {
    return R.style.TextSecure_DarkRegistrationTheme;
  }
}
