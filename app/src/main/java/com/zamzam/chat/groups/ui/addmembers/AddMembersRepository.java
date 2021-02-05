package com.zamzam.chat.groups.ui.addmembers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.zamzam.chat.contacts.SelectedContact;
import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.recipients.RecipientId;
import com.zamzam.chat.util.concurrent.SignalExecutors;

class AddMembersRepository {

  private final Context context;

  AddMembersRepository() {
    this.context = ApplicationDependencies.getApplication();
  }

  void getOrCreateRecipientId(@NonNull SelectedContact selectedContact, @NonNull Consumer<RecipientId> consumer) {
    SignalExecutors.BOUNDED.execute(() -> consumer.accept(selectedContact.getOrCreateRecipientId(context)));
  }
}
