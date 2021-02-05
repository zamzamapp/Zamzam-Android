package com.zamzam.chat.notifications;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.zamzam.chat.MainActivity;
import com.zamzam.chat.R;
import com.zamzam.chat.database.RecipientDatabase;
import com.zamzam.chat.preferences.widgets.NotificationPrivacyPreference;
import com.zamzam.chat.util.TextSecurePreferences;

public class PendingMessageNotificationBuilder extends AbstractNotificationBuilder {

  public PendingMessageNotificationBuilder(Context context, NotificationPrivacyPreference privacy) {
    super(context, privacy);

    // TODO [greyson] Navigation
    Intent intent = new Intent(context, MainActivity.class);

    setSmallIcon(R.drawable.ic_notification);
    setColor(context.getResources().getColor(R.color.core_ultramarine));
    setCategory(NotificationCompat.CATEGORY_MESSAGE);

    setContentTitle(context.getString(R.string.MessageNotifier_you_may_have_new_messages));
    setContentText(context.getString(R.string.MessageNotifier_open_signal_to_check_for_recent_notifications));
    setTicker(context.getString(R.string.MessageNotifier_open_signal_to_check_for_recent_notifications));

    setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));
    setAutoCancel(true);
    setAlarms(null, RecipientDatabase.VibrateState.DEFAULT);

    setOnlyAlertOnce(true);

    if (!NotificationChannels.supported()) {
      setPriority(TextSecurePreferences.getNotificationPriority(context));
    }
  }
}
