package com.zamzam.chat.groups.ui;

import androidx.annotation.NonNull;

import com.zamzam.chat.recipients.Recipient;

public interface RecipientClickListener {
  void onClick(@NonNull Recipient recipient);
}
