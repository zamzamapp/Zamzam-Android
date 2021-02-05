package com.zamzam.chat.dependencies;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import com.zamzam.chat.BuildConfig;
import com.zamzam.chat.jobmanager.impl.FactoryJobPredicate;
import com.zamzam.chat.jobs.MarkerJob;
import com.zamzam.chat.jobs.PushDecryptMessageJob;
import com.zamzam.chat.jobs.PushGroupSendJob;
import com.zamzam.chat.jobs.PushMediaSendJob;
import com.zamzam.chat.jobs.PushProcessMessageJob;
import com.zamzam.chat.jobs.PushTextSendJob;
import com.zamzam.chat.jobs.ReactionSendJob;
import com.zamzam.chat.jobs.TypingSendJob;
import com.zamzam.chat.messages.IncomingMessageProcessor;
import com.zamzam.chat.crypto.storage.SignalProtocolStoreImpl;
import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.events.ReminderUpdateEvent;
import com.zamzam.chat.messages.BackgroundMessageRetriever;
import com.zamzam.chat.jobmanager.JobManager;
import com.zamzam.chat.jobmanager.JobMigrator;
import com.zamzam.chat.jobmanager.impl.JsonDataSerializer;
import com.zamzam.chat.jobs.FastJobStorage;
import com.zamzam.chat.jobs.JobManagerFactories;
import com.zamzam.chat.keyvalue.KeyValueStore;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.megaphone.MegaphoneRepository;
import com.zamzam.chat.notifications.DefaultMessageNotifier;
import com.zamzam.chat.notifications.MessageNotifier;
import com.zamzam.chat.notifications.OptimizedMessageNotifier;
import com.zamzam.chat.push.SecurityEventListener;
import com.zamzam.chat.push.SignalServiceNetworkAccess;
import com.zamzam.chat.recipients.LiveRecipientCache;
import com.zamzam.chat.messages.IncomingMessageObserver;
import com.zamzam.chat.util.AlarmSleepTimer;
import com.zamzam.chat.util.EarlyMessageCache;
import com.zamzam.chat.util.FeatureFlags;
import com.zamzam.chat.util.FrameRateTracker;
import com.zamzam.chat.util.TextSecurePreferences;
import com.zamzam.chat.util.concurrent.SignalExecutors;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.groupsv2.ClientZkOperations;
import org.whispersystems.signalservice.api.groupsv2.GroupsV2Operations;
import org.whispersystems.signalservice.api.util.CredentialsProvider;
import org.whispersystems.signalservice.api.util.SleepTimer;
import org.whispersystems.signalservice.api.util.UptimeSleepTimer;
import org.whispersystems.signalservice.api.websocket.ConnectivityListener;

import java.util.UUID;

/**
 * Implementation of {@link ApplicationDependencies.Provider} that provides real app dependencies.
 */
public class ApplicationDependencyProvider implements ApplicationDependencies.Provider {

  private static final String TAG = Log.tag(ApplicationDependencyProvider.class);

  private final Application                context;
  private final SignalServiceNetworkAccess networkAccess;

  public ApplicationDependencyProvider(@NonNull Application context, @NonNull SignalServiceNetworkAccess networkAccess) {
    this.context       = context;
    this.networkAccess = networkAccess;
  }

  private @NonNull ClientZkOperations provideClientZkOperations() {
    return ClientZkOperations.create(networkAccess.getConfiguration(context));
  }

  @Override
  public @NonNull GroupsV2Operations provideGroupsV2Operations() {
    return new GroupsV2Operations(provideClientZkOperations());
  }

  @Override
  public @NonNull SignalServiceAccountManager provideSignalServiceAccountManager() {
    return new SignalServiceAccountManager(networkAccess.getConfiguration(context),
                                           new DynamicCredentialsProvider(context),
                                           BuildConfig.SIGNAL_AGENT,
                                           provideGroupsV2Operations());
  }

  @Override
  public @NonNull SignalServiceMessageSender provideSignalServiceMessageSender() {
      return new SignalServiceMessageSender(networkAccess.getConfiguration(context),
                                            new DynamicCredentialsProvider(context),
                                            new SignalProtocolStoreImpl(context),
                                            BuildConfig.SIGNAL_AGENT,
                                            TextSecurePreferences.isMultiDevice(context),
                                            FeatureFlags.attachmentsV3(),
                                            Optional.fromNullable(IncomingMessageObserver.getPipe()),
                                            Optional.fromNullable(IncomingMessageObserver.getUnidentifiedPipe()),
                                            Optional.of(new SecurityEventListener(context)),
                                            provideClientZkOperations().getProfileOperations(),
                                            SignalExecutors.newCachedBoundedExecutor("signal-messages", 1, 16));
  }

  @Override
  public @NonNull SignalServiceMessageReceiver provideSignalServiceMessageReceiver() {
    SleepTimer sleepTimer = TextSecurePreferences.isFcmDisabled(context) ? new AlarmSleepTimer(context)
                                                                         : new UptimeSleepTimer();
    return new SignalServiceMessageReceiver(networkAccess.getConfiguration(context),
                                            new DynamicCredentialsProvider(context),
                                            BuildConfig.SIGNAL_AGENT,
                                            new PipeConnectivityListener(),
                                            sleepTimer,
                                            provideClientZkOperations().getProfileOperations());
  }

  @Override
  public @NonNull SignalServiceNetworkAccess provideSignalServiceNetworkAccess() {
    return networkAccess;
  }

  @Override
  public @NonNull IncomingMessageProcessor provideIncomingMessageProcessor() {
    return new IncomingMessageProcessor(context);
  }

  @Override
  public @NonNull BackgroundMessageRetriever provideBackgroundMessageRetriever() {
    return new BackgroundMessageRetriever();
  }

  @Override
  public @NonNull LiveRecipientCache provideRecipientCache() {
    return new LiveRecipientCache(context);
  }

  @Override
  public @NonNull JobManager provideJobManager() {
    return new JobManager(context, new JobManager.Configuration.Builder()
                                                               .setDataSerializer(new JsonDataSerializer())
                                                               .setJobFactories(JobManagerFactories.getJobFactories(context))
                                                               .setConstraintFactories(JobManagerFactories.getConstraintFactories(context))
                                                               .setConstraintObservers(JobManagerFactories.getConstraintObservers(context))
                                                               .setJobStorage(new FastJobStorage(DatabaseFactory.getJobDatabase(context), SignalExecutors.newCachedSingleThreadExecutor("signal-fast-job-storage")))
                                                               .setJobMigrator(new JobMigrator(TextSecurePreferences.getJobManagerVersion(context), JobManager.CURRENT_VERSION, JobManagerFactories.getJobMigrations(context)))
                                                               .addReservedJobRunner(new FactoryJobPredicate(PushDecryptMessageJob.KEY, PushProcessMessageJob.KEY, MarkerJob.KEY))
                                                               .addReservedJobRunner(new FactoryJobPredicate(PushTextSendJob.KEY, PushMediaSendJob.KEY, PushGroupSendJob.KEY, ReactionSendJob.KEY, TypingSendJob.KEY))
                                                               .build());
  }

  @Override
  public @NonNull FrameRateTracker provideFrameRateTracker() {
    return new FrameRateTracker(context);
  }

  @Override
  public @NonNull KeyValueStore provideKeyValueStore() {
    return new KeyValueStore(context);
  }

  @Override
  public @NonNull MegaphoneRepository provideMegaphoneRepository() {
    return new MegaphoneRepository(context);
  }

  @Override
  public @NonNull EarlyMessageCache provideEarlyMessageCache() {
    return new EarlyMessageCache();
  }

  @Override
  public @NonNull MessageNotifier provideMessageNotifier() {
    return new OptimizedMessageNotifier(new DefaultMessageNotifier());
  }

  @Override
  public @NonNull IncomingMessageObserver provideIncomingMessageObserver() {
    return new IncomingMessageObserver(context);
  }

  private static class DynamicCredentialsProvider implements CredentialsProvider {

    private final Context context;

    private DynamicCredentialsProvider(Context context) {
      this.context = context.getApplicationContext();
    }

    @Override
    public UUID getUuid() {
      return TextSecurePreferences.getLocalUuid(context);
    }

    @Override
    public String getE164() {
      return TextSecurePreferences.getLocalNumber(context);
    }

    @Override
    public String getPassword() {
      return TextSecurePreferences.getPushServerPassword(context);
    }

    @Override
    public String getSignalingKey() {
      return TextSecurePreferences.getSignalingKey(context);
    }
  }

  private class PipeConnectivityListener implements ConnectivityListener {

    @Override
    public void onConnected() {
      Log.i(TAG, "onConnected()");
      TextSecurePreferences.setUnauthorizedReceived(context, false);
    }

    @Override
    public void onConnecting() {
      Log.i(TAG, "onConnecting()");
    }

    @Override
    public void onDisconnected() {
      Log.w(TAG, "onDisconnected()");
    }

    @Override
    public void onAuthenticationFailure() {
      Log.w(TAG, "onAuthenticationFailure()");
      TextSecurePreferences.setUnauthorizedReceived(context, true);
      EventBus.getDefault().post(new ReminderUpdateEvent());
    }
  }
}
