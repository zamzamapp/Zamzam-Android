package com.zamzam.chat.jobs;

import androidx.annotation.NonNull;

import org.signal.zkgroup.InvalidInputException;
import org.signal.zkgroup.groups.GroupMasterKey;
import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.database.GroupDatabase;
import com.zamzam.chat.database.MmsDatabase;
import com.zamzam.chat.database.model.databaseprotos.DecryptedGroupV2Context;
import com.zamzam.chat.groups.GroupChangeBusyException;
import com.zamzam.chat.groups.GroupId;
import com.zamzam.chat.groups.GroupManager;
import com.zamzam.chat.groups.GroupNotAMemberException;
import com.zamzam.chat.groups.GroupProtoUtil;
import com.zamzam.chat.groups.v2.processing.GroupsV2StateProcessor;
import com.zamzam.chat.jobmanager.Data;
import com.zamzam.chat.jobmanager.Job;
import com.zamzam.chat.jobmanager.impl.NetworkConstraint;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.mms.MmsException;
import com.zamzam.chat.mms.OutgoingGroupUpdateMessage;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.util.Hex;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.groupsv2.NoCredentialForRedemptionTimeException;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Use to create and show a thread for an unknown GV2 group.
 */
public final class WakeGroupV2Job extends BaseJob {

  public static final String KEY = "WakeGroupV2Job";

  private static final String TAG = Log.tag(WakeGroupV2Job.class);

  private static final String KEY_GROUP_MASTER_KEY = "group_id";

  private final GroupMasterKey groupMasterKey;

  public WakeGroupV2Job(@NonNull GroupMasterKey groupMasterKey) {
    this(new Parameters.Builder()
                       .setQueue("RequestGroupV2InfoJob::" + GroupId.v2(groupMasterKey))
                       .addConstraint(NetworkConstraint.KEY)
                       .setLifespan(TimeUnit.DAYS.toMillis(1))
                       .setMaxAttempts(Parameters.UNLIMITED)
                       .build(),
         groupMasterKey);
  }

  private WakeGroupV2Job(@NonNull Parameters parameters, @NonNull GroupMasterKey groupMasterKey) {
    super(parameters);

    this.groupMasterKey = groupMasterKey;
  }

  @Override
  public @NonNull Data serialize() {
    return new Data.Builder().putString(KEY_GROUP_MASTER_KEY, Hex.toStringCondensed(groupMasterKey.serialize()))
                             .build();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onRun() throws IOException, GroupNotAMemberException, GroupChangeBusyException {
    Log.i(TAG, "Waking group");

    GroupDatabase groupDatabase = DatabaseFactory.getGroupDatabase(context);
    GroupId.V2    groupId       = GroupId.v2(groupMasterKey);

    if (!groupDatabase.getGroup(groupId).isPresent()) {
      GroupManager.updateGroupFromServer(context, groupMasterKey, GroupsV2StateProcessor.LATEST, System.currentTimeMillis(), null);
      Log.i(TAG, "Group created " + groupId);
    } else {
      Log.w(TAG, "Group already exists " + groupId);
    }

    Optional<GroupDatabase.GroupRecord> group = groupDatabase.getGroup(groupId);
    if (!group.isPresent()) {
      Log.w(TAG, "Failed to create group from server " + groupId);
      return;
    }

    Log.i(TAG, "Waking group " + groupId);
    try {
      Recipient                       groupRecipient          = Recipient.externalGroup(context, groupId);
      long                            threadId                = DatabaseFactory.getThreadDatabase(context).getThreadIdFor(groupRecipient);
      GroupDatabase.V2GroupProperties v2GroupProperties       = group.get().requireV2GroupProperties();
      DecryptedGroupV2Context         decryptedGroupV2Context = GroupProtoUtil.createDecryptedGroupV2Context(v2GroupProperties.getGroupMasterKey(), v2GroupProperties.getDecryptedGroup(), null, null);
      MmsDatabase                     mmsDatabase             = DatabaseFactory.getMmsDatabase(context);
      OutgoingGroupUpdateMessage      outgoingMessage         = new OutgoingGroupUpdateMessage(groupRecipient, decryptedGroupV2Context, null, System.currentTimeMillis(), 0, false, null, Collections.emptyList(), Collections.emptyList());

      long messageId = mmsDatabase.insertMessageOutbox(outgoingMessage, threadId, false, null);

      mmsDatabase.markAsSent(messageId, true);
    } catch (MmsException e) {
      Log.w(TAG, e);
    }
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception e) {
    return e instanceof PushNetworkException ||
           e instanceof NoCredentialForRedemptionTimeException ||
           e instanceof GroupChangeBusyException;
  }

  @Override
  public void onFailure() {
  }

  public static final class Factory implements Job.Factory<WakeGroupV2Job> {

    @Override
    public @NonNull WakeGroupV2Job create(@NonNull Parameters parameters, @NonNull Data data) {
      try {
        return new WakeGroupV2Job(parameters,
                                  new GroupMasterKey(Hex.fromStringCondensed(data.getString(KEY_GROUP_MASTER_KEY))));
      } catch (InvalidInputException | IOException e) {
        throw new AssertionError(e);
      }
    }
  }
}
