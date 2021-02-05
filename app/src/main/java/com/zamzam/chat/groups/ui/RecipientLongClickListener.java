package com.zamzam.chat.groups.ui;

import androidx.annotation.NonNull;

import com.zamzam.chat.recipients.Recipient;

public interface RecipientLongClickListener {
  boolean onLongClick(@NonNull Recipient recipient);
}
