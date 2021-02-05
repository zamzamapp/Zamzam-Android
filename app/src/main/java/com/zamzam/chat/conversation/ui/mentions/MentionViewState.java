package com.zamzam.chat.conversation.ui.mentions;

import android.content.Context;

import androidx.annotation.NonNull;

import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.util.MappingModel;

import java.util.Objects;

public final class MentionViewState implements MappingModel<MentionViewState> {

  private final Recipient recipient;

  public MentionViewState(@NonNull Recipient recipient) {
    this.recipient = recipient;
  }

  @NonNull String getName(@NonNull Context context) {
    return recipient.getDisplayName(context);
  }

  @NonNull Recipient getRecipient() {
    return recipient;
  }

  @Override
  public boolean areItemsTheSame(@NonNull MentionViewState newItem) {
    return recipient.getId().equals(newItem.recipient.getId());
  }

  @Override
  public boolean areContentsTheSame(@NonNull MentionViewState newItem) {
    Context context = ApplicationDependencies.getApplication();
    return recipient.getDisplayName(context).equals(newItem.recipient.getDisplayName(context)) &&
           Objects.equals(recipient.getProfileAvatar(), newItem.recipient.getProfileAvatar());
  }
}
