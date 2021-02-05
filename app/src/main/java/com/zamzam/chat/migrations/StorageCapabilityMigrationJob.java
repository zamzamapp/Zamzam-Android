package com.zamzam.chat.migrations;

import androidx.annotation.NonNull;

import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.jobmanager.Data;
import com.zamzam.chat.jobmanager.Job;
import com.zamzam.chat.jobmanager.JobManager;
import com.zamzam.chat.jobs.MultiDeviceKeysUpdateJob;
import com.zamzam.chat.jobs.MultiDeviceStorageSyncRequestJob;
import com.zamzam.chat.jobs.RefreshAttributesJob;
import com.zamzam.chat.jobs.StorageForcePushJob;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.util.TextSecurePreferences;

/**
 * This does a couple things:
 *   (1) Sets the storage capability for reglockv2 users by refreshing account attributes.
 *   (2) Force-pushes storage, which is now backed by the KBS master key.
 *
 * Note: *All* users need to do this force push, because some people were in the storage service FF
 *       bucket in the past, and if we don't schedule a force push, they could enter a situation
 *       where different storage items are encrypted with different keys.
 */
public class StorageCapabilityMigrationJob extends MigrationJob {

  private static final String TAG = Log.tag(StorageCapabilityMigrationJob.class);

  public static final String KEY = "StorageCapabilityMigrationJob";

  StorageCapabilityMigrationJob() {
    this(new Parameters.Builder().build());
  }

  private StorageCapabilityMigrationJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  public boolean isUiBlocking() {
    return false;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void performMigration() {
    JobManager jobManager = ApplicationDependencies.getJobManager();

    jobManager.add(new RefreshAttributesJob());

    if (TextSecurePreferences.isMultiDevice(context)) {
      Log.i(TAG, "Multi-device.");
      jobManager.startChain(new StorageForcePushJob())
                .then(new MultiDeviceKeysUpdateJob())
                .then(new MultiDeviceStorageSyncRequestJob())
                .enqueue();
    } else {
      Log.i(TAG, "Single-device.");
      jobManager.add(new StorageForcePushJob());
    }
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return false;
  }

  public static class Factory implements Job.Factory<StorageCapabilityMigrationJob> {
    @Override
    public @NonNull StorageCapabilityMigrationJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new StorageCapabilityMigrationJob(parameters);
    }
  }
}
