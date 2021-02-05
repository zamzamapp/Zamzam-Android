package com.zamzam.chat.jobs;

import androidx.annotation.NonNull;

import org.signal.zkgroup.profiles.ProfileKey;
import com.zamzam.chat.crypto.ProfileKeyUtil;
import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.groups.GroupId;
import com.zamzam.chat.jobmanager.Data;
import com.zamzam.chat.jobmanager.Job;
import com.zamzam.chat.recipients.Recipient;

import java.util.List;

public class RotateProfileKeyJob extends BaseJob {

  public static String KEY = "RotateProfileKeyJob";

  public RotateProfileKeyJob() {
    this(new Job.Parameters.Builder()
                           .setQueue("__ROTATE_PROFILE_KEY__")
                           .setMaxInstances(2)
                           .build());
  }

  private RotateProfileKeyJob(@NonNull Job.Parameters parameters) {
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
  public void onRun() {
    ProfileKey newProfileKey = ProfileKeyUtil.createNew();
    Recipient  self          = Recipient.self();

    DatabaseFactory.getRecipientDatabase(context).setProfileKey(self.getId(), newProfileKey);

    ApplicationDependencies.getJobManager().add(new ProfileUploadJob());
    ApplicationDependencies.getJobManager().add(new RefreshAttributesJob());

    updateProfileKeyOnAllV2Groups();
  }

  private void updateProfileKeyOnAllV2Groups() {
    List<GroupId.V2> allGv2Groups = DatabaseFactory.getGroupDatabase(context).getAllGroupV2Ids();

    for (GroupId.V2 groupId : allGv2Groups) {
      ApplicationDependencies.getJobManager().add(new GroupV2UpdateSelfProfileKeyJob(groupId));
    }
  }

  @Override
  public void onFailure() {
  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception exception) {
    return false;
  }

  public static final class Factory implements Job.Factory<RotateProfileKeyJob> {
    @Override
    public @NonNull RotateProfileKeyJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new RotateProfileKeyJob(parameters);
    }
  }
}
