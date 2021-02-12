package com.zamzam.chat.groups.ui.managegroup.dialogs;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.zamzam.chat.R;
import com.zamzam.chat.groups.ui.GroupMemberEntry;
import com.zamzam.chat.groups.ui.GroupMemberListView;
import com.zamzam.chat.recipients.Recipient;

import java.util.ArrayList;
import java.util.List;

public final class GroupInviteSentDialog {

  private GroupInviteSentDialog() {
  }

  public static @Nullable Dialog showInvitesSent(@NonNull Context context, @NonNull List<Recipient> recipients) {
    int size = recipients.size();
    if (size == 0) {
      return null;
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                                 .setTitle(context.getResources().getQuantityString(R.plurals.GroupManagement_invitation_sent, size, size))
                                                 // TODO: GV2 Need a URL for learn more
                                                 //  .setNegativeButton(R.string.GroupManagement_learn_more, (dialog, which) -> {
                                                 //  })
                                                 .setPositiveButton(android.R.string.ok, null);
    if (size == 1) {
      builder.setMessage(context.getString(R.string.GroupManagement_invite_single_user, recipients.get(0).getDisplayName(context)));
    } else {
      builder.setMessage(R.string.GroupManagement_invite_multiple_users)
             .setView(R.layout.dialog_multiple_group_invites_sent);
    }

    Dialog dialog = builder.show();
    if (size > 1) {
      GroupMemberListView invitees = dialog.findViewById(R.id.list_invitees);

      List<GroupMemberEntry.PendingMember> pendingMembers = new ArrayList<>(recipients.size());
      for (Recipient r : recipients) {
        pendingMembers.add(new GroupMemberEntry.PendingMember(r));
      }

      //noinspection ConstantConditions
      invitees.setMembers(pendingMembers);
    }

    return dialog;
  }
}