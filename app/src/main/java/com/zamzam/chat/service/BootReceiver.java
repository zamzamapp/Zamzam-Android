package com.zamzam.chat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.jobs.PushNotificationReceiveJob;

public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    ApplicationDependencies.getJobManager().add(new PushNotificationReceiveJob(context));
  }
}
