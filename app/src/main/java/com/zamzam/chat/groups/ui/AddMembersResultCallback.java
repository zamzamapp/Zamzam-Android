package com.zamzam.chat.groups.ui;

import androidx.annotation.NonNull;

import com.zamzam.chat.recipients.RecipientId;

import java.util.List;

public interface AddMembersResultCallback {
  void onMembersAdded(int numberOfMembersAdded, @NonNull List<RecipientId> invitedMembers);
}
