package com.zamzam.chat.migrations;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.jobmanager.Data;
import com.zamzam.chat.jobmanager.Job;
import com.zamzam.chat.jobmanager.impl.NetworkConstraint;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.recipients.RecipientId;
import com.zamzam.chat.util.TextSecurePreferences;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;

import java.io.IOException;
import java.util.UUID;

/**
 * Couple migrations steps need to happen after we move to UUIDS.
 *  - We need to get our own UUID.
 *  - We need to fetch the new UUID sealed sender cert.
 *  - We need to do a directory sync so we can guarantee that all active users have UUIDs.
 */
public class UuidMigrationJob extends MigrationJob {

  public static final String KEY = "UuidMigrationJob";

  private static final String TAG = Log.tag(UuidMigrationJob.class);

  UuidMigrationJob() {
    this(new Parameters.Builder().addConstraint(NetworkConstraint.KEY).build());
  }

  private UuidMigrationJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  boolean isUiBlocking() {
    return false;
  }

  @Override
  void performMigration() throws Exception {
    if (!TextSecurePreferences.isPushRegistered(context) || TextUtils.isEmpty(TextSecurePreferences.getLocalNumber(context))) {
      Log.w(TAG, "Not registered! Skipping migration, as it wouldn't do anything.");
      return;
    }

    ensureSelfRecipientExists(context);
    fetchOwnUuid(context);
    rotateSealedSenderCerts(context);
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return e instanceof IOException;
  }

  private static void ensureSelfRecipientExists(@NonNull Context context) {
    DatabaseFactory.getRecipientDatabase(context).getOrInsertFromE164(TextSecurePreferences.getLocalNumber(context));
  }

  private static void fetchOwnUuid(@NonNull Context context) throws IOException {
    RecipientId self      = Recipient.self().getId();
    UUID        localUuid = ApplicationDependencies.getSignalServiceAccountManager().getOwnUuid();

    DatabaseFactory.getRecipientDatabase(context).markRegisteredOrThrow(self, localUuid);
    TextSecurePreferences.setLocalUuid(context, localUuid);
  }

  private static void rotateSealedSenderCerts(@NonNull Context context) throws IOException {
    SignalServiceAccountManager accountManager = ApplicationDependencies.getSignalServiceAccountManager();
    byte[]                      certificate    = accountManager.getSenderCertificate();

    TextSecurePreferences.setUnidentifiedAccessCertificate(context, certificate);
  }


  public static class Factory implements Job.Factory<UuidMigrationJob> {
    @Override
    public @NonNull UuidMigrationJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new UuidMigrationJob(parameters);
    }
  }
}
