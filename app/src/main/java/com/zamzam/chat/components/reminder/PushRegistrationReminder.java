package com.zamzam.chat.components.reminder;

import android.content.Context;

import com.zamzam.chat.R;
import com.zamzam.chat.registration.RegistrationNavigationActivity;
import com.zamzam.chat.util.TextSecurePreferences;

public class PushRegistrationReminder extends Reminder {

  public PushRegistrationReminder(final Context context) {
    super(context.getString(R.string.reminder_header_push_title),
          context.getString(R.string.reminder_header_push_text));

    setOkListener(v -> context.startActivity(RegistrationNavigationActivity.newIntentForReRegistration(context)));
  }

  @Override
  public boolean isDismissable() {
    return false;
  }

  public static boolean isEligible(Context context) {
    return !TextSecurePreferences.isPushRegistered(context);
  }
}
