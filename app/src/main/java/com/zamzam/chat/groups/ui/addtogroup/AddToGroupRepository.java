package com.zamzam.chat.groups.ui.addtogroup;

import android.content.Context;

import androidx.annotation.NonNull;

import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.groups.GroupChangeException;
import com.zamzam.chat.groups.GroupId;
import com.zamzam.chat.groups.GroupManager;
import com.zamzam.chat.groups.MembershipNotSuitableForV2Exception;
import com.zamzam.chat.groups.ui.GroupChangeErrorCallback;
import com.zamzam.chat.groups.ui.GroupChangeFailureReason;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.recipients.RecipientId;
import com.zamzam.chat.util.concurrent.SignalExecutors;

import java.io.IOException;
import java.util.Collections;

final class AddToGroupRepository {

  private static final String TAG = Log.tag(AddToGroupRepository.class);

  private final Context context;

  AddToGroupRepository() {
    this.context = ApplicationDependencies.getApplication();
  }

  public void add(@NonNull RecipientId recipientId,
                  @NonNull Recipient groupRecipient,
                  @NonNull GroupChangeErrorCallback error,
                  @NonNull Runnable success)
  {
    SignalExecutors.UNBOUNDED.execute(() -> {
      try {
        GroupId.Push pushGroupId = groupRecipient.requireGroupId().requirePush();

        GroupManager.addMembers(context, pushGroupId, Collections.singletonList(recipientId));

        success.run();
        } catch (GroupChangeException | MembershipNotSuitableForV2Exception | IOException e) {
        Log.w(TAG, e);
        error.onError(GroupChangeFailureReason.fromException(e));
      }
    });
  }
}
