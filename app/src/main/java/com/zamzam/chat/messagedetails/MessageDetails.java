package com.zamzam.chat.messagedetails;

import androidx.annotation.NonNull;

import com.annimon.stream.ComparatorCompat;

import com.zamzam.chat.database.model.MessageRecord;
import com.zamzam.chat.dependencies.ApplicationDependencies;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

final class MessageDetails {
  private static final Comparator<RecipientDeliveryStatus> HAS_DISPLAY_NAME     = (r1, r2) -> Boolean.compare(r2.getRecipient().hasAUserSetDisplayName(ApplicationDependencies.getApplication()), r1.getRecipient().hasAUserSetDisplayName(ApplicationDependencies.getApplication()));
  private static final Comparator<RecipientDeliveryStatus> ALPHABETICAL         = (r1, r2) -> r1.getRecipient().getDisplayName(ApplicationDependencies.getApplication()).compareToIgnoreCase(r2.getRecipient().getDisplayName(ApplicationDependencies.getApplication()));
  private static final Comparator<RecipientDeliveryStatus> RECIPIENT_COMPARATOR = ComparatorCompat.chain(HAS_DISPLAY_NAME).thenComparing(ALPHABETICAL);

  private final MessageRecord messageRecord;

  private final Collection<RecipientDeliveryStatus> pending;
  private final Collection<RecipientDeliveryStatus> sent;
  private final Collection<RecipientDeliveryStatus> delivered;
  private final Collection<RecipientDeliveryStatus> read;
  private final Collection<RecipientDeliveryStatus> notSent;

  MessageDetails(MessageRecord messageRecord, List<RecipientDeliveryStatus> recipients) {
    this.messageRecord = messageRecord;

    pending   = new TreeSet<>(RECIPIENT_COMPARATOR);
    sent      = new TreeSet<>(RECIPIENT_COMPARATOR);
    delivered = new TreeSet<>(RECIPIENT_COMPARATOR);
    read      = new TreeSet<>(RECIPIENT_COMPARATOR);
    notSent   = new TreeSet<>(RECIPIENT_COMPARATOR);

    if (messageRecord.isOutgoing()) {
      for (RecipientDeliveryStatus status : recipients) {
        switch (status.getDeliveryStatus()) {
          case UNKNOWN:
            notSent.add(status);
            break;
          case PENDING:
            pending.add(status);
            break;
          case SENT:
            sent.add(status);
            break;
          case DELIVERED:
            delivered.add(status);
            break;
          case READ:
            read.add(status);
            break;
        }
      }
    } else {
      sent.addAll(recipients);
    }
  }

  @NonNull MessageRecord getMessageRecord() {
    return messageRecord;
  }

  @NonNull Collection<RecipientDeliveryStatus> getPending() {
    return pending;
  }

  @NonNull Collection<RecipientDeliveryStatus> getSent() {
    return sent;
  }

  @NonNull Collection<RecipientDeliveryStatus> getDelivered() {
    return delivered;
  }

  @NonNull Collection<RecipientDeliveryStatus> getRead() {
    return read;
  }

  @NonNull Collection<RecipientDeliveryStatus> getNotSent() {
    return notSent;
  }
}
