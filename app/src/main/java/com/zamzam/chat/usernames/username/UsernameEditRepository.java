package com.zamzam.chat.usernames.username;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.util.TextSecurePreferences;
import com.zamzam.chat.util.concurrent.SignalExecutors;

import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.push.exceptions.UsernameMalformedException;
import org.whispersystems.signalservice.api.push.exceptions.UsernameTakenException;

import java.io.IOException;
import java.util.concurrent.Executor;

class UsernameEditRepository {

  private static final String TAG = Log.tag(UsernameEditRepository.class);

  private final Application                 application;
  private final SignalServiceAccountManager accountManager;
  private final Executor                    executor;

  UsernameEditRepository() {
    this.application    = ApplicationDependencies.getApplication();
    this.accountManager = ApplicationDependencies.getSignalServiceAccountManager();
    this.executor       = SignalExecutors.UNBOUNDED;
  }

  void setUsername(@NonNull String username, @NonNull Callback<UsernameSetResult> callback) {
    executor.execute(() -> callback.onComplete(setUsernameInternal(username)));
  }

  void deleteUsername(@NonNull Callback<UsernameDeleteResult> callback) {
    executor.execute(() -> callback.onComplete(deleteUsernameInternal()));
  }

  @WorkerThread
  private @NonNull UsernameSetResult setUsernameInternal(@NonNull String username) {
    try {
      accountManager.setUsername(username);
      TextSecurePreferences.setLocalUsername(application, username);
      DatabaseFactory.getRecipientDatabase(application).setUsername(Recipient.self().getId(), username);
      Log.i(TAG, "[setUsername] Successfully set username.");
      return UsernameSetResult.SUCCESS;
    } catch (UsernameTakenException e) {
      Log.w(TAG, "[setUsername] Username taken.");
      return UsernameSetResult.USERNAME_UNAVAILABLE;
    } catch (UsernameMalformedException e) {
      Log.w(TAG, "[setUsername] Username malformed.");
      return UsernameSetResult.USERNAME_INVALID;
    } catch (IOException e) {
      Log.w(TAG, "[setUsername] Generic network exception.", e);
      return UsernameSetResult.NETWORK_ERROR;
    }
  }

  @WorkerThread
  private @NonNull UsernameDeleteResult deleteUsernameInternal() {
    try {
      accountManager.deleteUsername();
      TextSecurePreferences.setLocalUsername(application, null);
      DatabaseFactory.getRecipientDatabase(application).setUsername(Recipient.self().getId(), null);
      Log.i(TAG, "[deleteUsername] Successfully deleted the username.");
      return UsernameDeleteResult.SUCCESS;
    } catch (IOException e) {
      Log.w(TAG, "[deleteUsername] Generic network exception.", e);
      return UsernameDeleteResult.NETWORK_ERROR;
    }
  }

  enum UsernameSetResult {
    SUCCESS, USERNAME_UNAVAILABLE, USERNAME_INVALID, NETWORK_ERROR
  }

  enum UsernameDeleteResult {
    SUCCESS, NETWORK_ERROR
  }

  enum UsernameAvailableResult {
    TRUE, FALSE, NETWORK_ERROR
  }

  interface Callback<E> {
    void onComplete(E result);
  }
}
