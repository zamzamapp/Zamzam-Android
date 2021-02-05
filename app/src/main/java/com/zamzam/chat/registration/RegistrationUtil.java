package com.zamzam.chat.registration;

import android.content.Context;

import androidx.annotation.NonNull;

import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.jobs.DirectoryRefreshJob;
import com.zamzam.chat.jobs.StorageSyncJob;
import com.zamzam.chat.keyvalue.SignalStore;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.util.TextSecurePreferences;

public final class RegistrationUtil {

  private static final String TAG = Log.tag(RegistrationUtil.class);

  private RegistrationUtil() {}

  /**
   * There's several events where a registration may or may not be considered complete based on what
   * path a user has taken. This will only truly mark registration as complete if all of the
   * requirements are met.
   */
  public static void maybeMarkRegistrationComplete(@NonNull Context context) {
    if (!SignalStore.registrationValues().isRegistrationComplete() &&
        TextSecurePreferences.isPushRegistered(context)            &&
        !Recipient.self().getProfileName().isEmpty()               &&
        (SignalStore.kbsValues().hasPin() || SignalStore.kbsValues().hasOptedOut()))
    {
      Log.i(TAG, "Marking registration completed.", new Throwable());
      SignalStore.registrationValues().setRegistrationComplete();
      ApplicationDependencies.getJobManager().startChain(new StorageSyncJob())
                                             .then(new DirectoryRefreshJob(false))
                                             .enqueue();
    } else if (!SignalStore.registrationValues().isRegistrationComplete()) {
      Log.i(TAG, "Registration is not yet complete.", new Throwable());
    }
  }
}
