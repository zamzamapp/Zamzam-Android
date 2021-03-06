package com.zamzam.chat.groups;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.jobmanager.Job;
import com.zamzam.chat.jobmanager.JobManager;
import com.zamzam.chat.jobs.RetrieveProfileJob;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.recipients.RecipientId;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class GroupsV2CapabilityChecker {

  private static final String TAG = Log.tag(GroupsV2CapabilityChecker.class);

  private GroupsV2CapabilityChecker() {}

  /**
   * @param resolved A collection of resolved recipients.
   */
  @WorkerThread
  public static void refreshCapabilitiesIfNecessary(@NonNull Collection<Recipient> resolved) throws IOException {
    Set<RecipientId> needsRefresh = Stream.of(resolved)
                                          .filter(r -> r.getGroupsV2Capability() != Recipient.Capability.SUPPORTED)
                                          .map(Recipient::getId)
                                          .collect(Collectors.toSet());

    if (needsRefresh.size() > 0) {
      Log.d(TAG, "[refreshCapabilitiesIfNecessary] Need to refresh " + needsRefresh.size() + " recipients.");

      List<Job>  jobs       = RetrieveProfileJob.forRecipients(needsRefresh);
      JobManager jobManager = ApplicationDependencies.getJobManager();

      for (Job job : jobs) {
        if (!jobManager.runSynchronously(job, TimeUnit.SECONDS.toMillis(5000)).isPresent()) {
          throw new IOException("Recipient capability was not retrieved in time");
        }
      }
    }
  }

  @WorkerThread
  static boolean allAndSelfSupportGroupsV2AndUuid(@NonNull Collection<RecipientId> recipientIds)
      throws IOException
  {
    HashSet<RecipientId> recipientIdsSet = new HashSet<>(recipientIds);

    recipientIdsSet.add(Recipient.self().getId());

    return allSupportGroupsV2AndUuid(recipientIdsSet);
  }

  @WorkerThread
  static boolean allSupportGroupsV2AndUuid(@NonNull Collection<RecipientId> recipientIds)
      throws IOException
  {
    Set<RecipientId> recipientIdsSet = new HashSet<>(recipientIds);
    refreshCapabilitiesIfNecessary(Recipient.resolvedList(recipientIdsSet));

    boolean noSelfGV2Support = false;
    int     noGv2Count       = 0;
    int     noUuidCount      = 0;

    for (RecipientId id : recipientIds) {
      Recipient            member        = Recipient.resolved(id);
      Recipient.Capability gv2Capability = member.getGroupsV2Capability();

      if (gv2Capability != Recipient.Capability.SUPPORTED) {
        Log.w(TAG, "At least one recipient does not support GV2, capability was " + gv2Capability);

        noGv2Count++;
        if (member.isLocalNumber()) {
          noSelfGV2Support = true;
        }
      }

      if (!member.hasUuid()) {
        noUuidCount++;
      }
    }

    if (noGv2Count + noUuidCount > 0) {
      if (noUuidCount > 0) {
        Log.w(TAG, noUuidCount + " recipient(s) did not have a UUID known to us");
      }
      if (noGv2Count > 0) {
        Log.w(TAG, noGv2Count + " recipient(s) do not support GV2");
        if (noSelfGV2Support) {
          Log.w(TAG, "Self does not support GV2");
        }
      }
      return false;
    }

    return true;
  }
}
