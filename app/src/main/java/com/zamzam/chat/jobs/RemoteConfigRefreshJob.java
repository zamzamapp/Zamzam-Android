package com.zamzam.chat.jobs;

import androidx.annotation.NonNull;

import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.jobmanager.Data;
import com.zamzam.chat.jobmanager.Job;
import com.zamzam.chat.jobmanager.impl.NetworkConstraint;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.util.FeatureFlags;
import com.zamzam.chat.util.TextSecurePreferences;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RemoteConfigRefreshJob extends BaseJob {

  private static final String TAG = Log.tag(RemoteConfigRefreshJob.class);

  public static final String KEY = "RemoteConfigRefreshJob";

  public RemoteConfigRefreshJob() {
    this(new Job.Parameters.Builder()
                           .setQueue("RemoteConfigRefreshJob")
                           .setMaxInstances(1)
                           .addConstraint(NetworkConstraint.KEY)
                           .setMaxAttempts(Parameters.UNLIMITED)
                           .setLifespan(TimeUnit.DAYS.toMillis(1))
                           .build());
  }

  private RemoteConfigRefreshJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  public @NonNull Data serialize() {
    return Data.EMPTY;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  protected void onRun() throws Exception {
    if (!TextSecurePreferences.isPushRegistered(context)) {
      Log.w(TAG, "Not registered. Skipping.");
      return;
    }

    Map<String, Object> config = ApplicationDependencies.getSignalServiceAccountManager().getRemoteConfig();
    FeatureFlags.update(config);
  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception e) {
    return e instanceof PushNetworkException;
  }

  @Override
  public void onFailure() {
  }

  public static final class Factory implements Job.Factory<RemoteConfigRefreshJob> {
    @Override
    public @NonNull RemoteConfigRefreshJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new RemoteConfigRefreshJob(parameters);
    }
  }
}
