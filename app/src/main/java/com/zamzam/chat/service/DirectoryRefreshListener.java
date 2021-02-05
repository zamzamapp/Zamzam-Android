package com.zamzam.chat.service;


import android.content.Context;
import android.content.Intent;

import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.jobs.DirectoryRefreshJob;
import com.zamzam.chat.util.TextSecurePreferences;

import java.util.concurrent.TimeUnit;

public class DirectoryRefreshListener extends PersistentAlarmManagerListener {

  private static final long INTERVAL = TimeUnit.HOURS.toMillis(12);

  @Override
  protected long getNextScheduledExecutionTime(Context context) {
    return TextSecurePreferences.getDirectoryRefreshTime(context);
  }

  @Override
  protected long onAlarm(Context context, long scheduledTime) {
    if (scheduledTime != 0 && TextSecurePreferences.isPushRegistered(context)) {
      ApplicationDependencies.getJobManager().add(new DirectoryRefreshJob(true));
    }

    long newTime = System.currentTimeMillis() + INTERVAL;
    TextSecurePreferences.setDirectoryRefreshTime(context, newTime);

    return newTime;
  }

  public static void schedule(Context context) {
    new DirectoryRefreshListener().onReceive(context, new Intent());
  }
}
