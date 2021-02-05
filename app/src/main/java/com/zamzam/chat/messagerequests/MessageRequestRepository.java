package com.zamzam.chat.messagerequests;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import org.signal.storageservice.protos.groups.local.DecryptedGroup;
import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.database.GroupDatabase;
import com.zamzam.chat.database.MessagingDatabase;
import com.zamzam.chat.database.RecipientDatabase;
import com.zamzam.chat.database.ThreadDatabase;
import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.groups.GroupChangeException;
import com.zamzam.chat.groups.GroupManager;
import com.zamzam.chat.groups.ui.GroupChangeErrorCallback;
import com.zamzam.chat.groups.ui.GroupChangeFailureReason;
import com.zamzam.chat.jobs.MultiDeviceMessageRequestResponseJob;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.notifications.MarkReadReceiver;
import com.zamzam.chat.recipients.LiveRecipient;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.recipients.RecipientId;
import com.zamzam.chat.recipients.RecipientUtil;
import com.zamzam.chat.sms.MessageSender;
import com.zamzam.chat.util.TextSecurePreferences;
import com.zamzam.chat.util.concurrent.SignalExecutors;
import org.whispersystems.libsignal.util.guava.Optional;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

final class MessageRequestRepository {

  private static final String TAG = Log.tag(MessageRequestRepository.class);

  private final Context  context;
  private final Executor executor;

  MessageRequestRepository(@NonNull Context context) {
    this.context  = context.getApplicationContext();
    this.executor = SignalExecutors.BOUNDED;
  }

  void getGroups(@NonNull RecipientId recipientId, @NonNull Consumer<List<String>> onGroupsLoaded) {
    executor.execute(() -> {
      GroupDatabase groupDatabase = DatabaseFactory.getGroupDatabase(context);
      onGroupsLoaded.accept(groupDatabase.getPushGroupNamesContainingMember(recipientId));
    });
  }

  void getMemberCount(@NonNull RecipientId recipientId, @NonNull Consumer<GroupMemberCount> onMemberCountLoaded) {
    executor.execute(() -> {
      GroupDatabase                       groupDatabase = DatabaseFactory.getGroupDatabase(context);
      Optional<GroupDatabase.GroupRecord> groupRecord   = groupDatabase.getGroup(recipientId);
      onMemberCountLoaded.accept(groupRecord.transform(record -> {
        if (record.isV2Group()) {
          DecryptedGroup decryptedGroup = record.requireV2GroupProperties().getDecryptedGroup();
          return new GroupMemberCount(decryptedGroup.getMembersCount(), decryptedGroup.getPendingMembersCount());
        } else {
          return new GroupMemberCount(record.getMembers().size(), 0);
        }
      }).or(GroupMemberCount.ZERO));
    });
  }

  void getMessageRequestState(@NonNull Recipient recipient, long threadId, @NonNull Consumer<MessageRequestState> state) {
    executor.execute(() -> {
      if (recipient.isPushV2Group()) {
        boolean pendingMember = DatabaseFactory.getGroupDatabase(context)
                                               .isPendingMember(recipient.requireGroupId().requireV2(), Recipient.self());
        state.accept(pendingMember ? MessageRequestState.UNACCEPTED
                                   : MessageRequestState.ACCEPTED);
      } else if (!RecipientUtil.isMessageRequestAccepted(context, threadId)) {
        state.accept(MessageRequestState.UNACCEPTED);
      } else if (RecipientUtil.isPreMessageRequestThread(context, threadId) && !RecipientUtil.isLegacyProfileSharingAccepted(recipient)) {
        state.accept(MessageRequestState.LEGACY);
      } else {
        state.accept(MessageRequestState.ACCEPTED);
      }
    });
  }

  void acceptMessageRequest(@NonNull LiveRecipient liveRecipient,
                            long threadId,
                            @NonNull Runnable onMessageRequestAccepted,
                            @NonNull GroupChangeErrorCallback error)
  {
    executor.execute(()-> {
      if (liveRecipient.get().isPushV2Group()) {
        try {
          Log.i(TAG, "GV2 accepting invite");
          GroupManager.acceptInvite(context, liveRecipient.get().requireGroupId().requireV2());

          RecipientDatabase recipientDatabase = DatabaseFactory.getRecipientDatabase(context);
          recipientDatabase.setProfileSharing(liveRecipient.getId(), true);

          onMessageRequestAccepted.run();
        } catch (GroupChangeException | IOException e) {
          Log.w(TAG, e);
          error.onError(GroupChangeFailureReason.fromException(e));
        }
      } else {
        RecipientDatabase recipientDatabase = DatabaseFactory.getRecipientDatabase(context);
        recipientDatabase.setProfileSharing(liveRecipient.getId(), true);

        MessageSender.sendProfileKey(context, threadId);

        List<MessagingDatabase.MarkedMessageInfo> messageIds = DatabaseFactory.getThreadDatabase(context)
                                                                              .setEntireThreadRead(threadId);
        ApplicationDependencies.getMessageNotifier().updateNotification(context);
        MarkReadReceiver.process(context, messageIds);

        if (TextSecurePreferences.isMultiDevice(context)) {
          ApplicationDependencies.getJobManager().add(MultiDeviceMessageRequestResponseJob.forAccept(liveRecipient.getId()));
        }

        onMessageRequestAccepted.run();
      }
    });
  }

  void deleteMessageRequest(@NonNull LiveRecipient recipient,
                            long threadId,
                            @NonNull Runnable onMessageRequestDeleted,
                            @NonNull GroupChangeErrorCallback error)
  {
    executor.execute(() -> {
      Recipient resolved = recipient.resolve();

      if (resolved.isGroup() && resolved.requireGroupId().isPush()) {
        try {
          GroupManager.leaveGroupFromBlockOrMessageRequest(context, resolved.requireGroupId().requirePush());
        } catch (GroupChangeException | IOException e) {
          Log.w(TAG, e);
          error.onError(GroupChangeFailureReason.fromException(e));
          return;
        }
      }

      if (TextSecurePreferences.isMultiDevice(context)) {
        ApplicationDependencies.getJobManager().add(MultiDeviceMessageRequestResponseJob.forDelete(recipient.getId()));
      }

      ThreadDatabase threadDatabase = DatabaseFactory.getThreadDatabase(context);
      threadDatabase.deleteConversation(threadId);

      onMessageRequestDeleted.run();
    });
  }

  void blockMessageRequest(@NonNull LiveRecipient liveRecipient,
                           @NonNull Runnable onMessageRequestBlocked,
                           @NonNull GroupChangeErrorCallback error)
  {
    executor.execute(() -> {
      Recipient recipient = liveRecipient.resolve();
      try {
        RecipientUtil.block(context, recipient);
      } catch (GroupChangeException | IOException e) {
        Log.w(TAG, e);
        error.onError(GroupChangeFailureReason.fromException(e));
        return;
      }
      liveRecipient.refresh();

      if (TextSecurePreferences.isMultiDevice(context)) {
        ApplicationDependencies.getJobManager().add(MultiDeviceMessageRequestResponseJob.forBlock(liveRecipient.getId()));
      }

      onMessageRequestBlocked.run();
    });
  }

  void blockAndDeleteMessageRequest(@NonNull LiveRecipient liveRecipient,
                                    long threadId,
                                    @NonNull Runnable onMessageRequestBlocked,
                                    @NonNull GroupChangeErrorCallback error)
  {
    executor.execute(() -> {
      Recipient recipient = liveRecipient.resolve();
      try{
        RecipientUtil.block(context, recipient);
      } catch (GroupChangeException | IOException e) {
        Log.w(TAG, e);
        error.onError(GroupChangeFailureReason.fromException(e));
        return;
      }
      liveRecipient.refresh();

      DatabaseFactory.getThreadDatabase(context).deleteConversation(threadId);

      if (TextSecurePreferences.isMultiDevice(context)) {
        ApplicationDependencies.getJobManager().add(MultiDeviceMessageRequestResponseJob.forBlockAndDelete(liveRecipient.getId()));
      }

      onMessageRequestBlocked.run();
    });
  }

  void unblockAndAccept(@NonNull LiveRecipient liveRecipient, long threadId, @NonNull Runnable onMessageRequestUnblocked) {
    executor.execute(() -> {
      Recipient         recipient         = liveRecipient.resolve();
      RecipientDatabase recipientDatabase = DatabaseFactory.getRecipientDatabase(context);

      RecipientUtil.unblock(context, recipient);
      recipientDatabase.setProfileSharing(liveRecipient.getId(), true);
      liveRecipient.refresh();

      List<MessagingDatabase.MarkedMessageInfo> messageIds = DatabaseFactory.getThreadDatabase(context)
                                                                            .setEntireThreadRead(threadId);
      ApplicationDependencies.getMessageNotifier().updateNotification(context);
      MarkReadReceiver.process(context, messageIds);

      if (TextSecurePreferences.isMultiDevice(context)) {
        ApplicationDependencies.getJobManager().add(MultiDeviceMessageRequestResponseJob.forAccept(liveRecipient.getId()));
      }

      onMessageRequestUnblocked.run();
    });
  }

  enum MessageRequestState {
    ACCEPTED, UNACCEPTED, LEGACY
  }
}
