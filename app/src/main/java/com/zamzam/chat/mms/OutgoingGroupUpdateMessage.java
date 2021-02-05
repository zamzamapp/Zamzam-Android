package com.zamzam.chat.mms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zamzam.chat.attachments.Attachment;
import com.zamzam.chat.contactshare.Contact;
import com.zamzam.chat.database.ThreadDatabase;
import com.zamzam.chat.database.model.databaseprotos.DecryptedGroupV2Context;
import com.zamzam.chat.linkpreview.LinkPreview;
import com.zamzam.chat.recipients.Recipient;
import org.whispersystems.signalservice.internal.push.SignalServiceProtos.GroupContext;

import java.util.Collections;
import java.util.List;

public final class OutgoingGroupUpdateMessage extends OutgoingSecureMediaMessage {

  private final MessageGroupContext messageGroupContext;

  public OutgoingGroupUpdateMessage(@NonNull Recipient recipient,
                                    @NonNull MessageGroupContext groupContext,
                                    @NonNull List<Attachment> avatar,
                                    long sentTimeMillis,
                                    long expiresIn,
                                    boolean viewOnce,
                                    @Nullable QuoteModel quote,
                                    @NonNull List<Contact> contacts,
                                    @NonNull List<LinkPreview> previews)
  {
    super(recipient, groupContext.getEncodedGroupContext(), avatar, sentTimeMillis,
          ThreadDatabase.DistributionTypes.CONVERSATION, expiresIn, viewOnce, quote, contacts, previews);

    this.messageGroupContext = groupContext;
  }

  public OutgoingGroupUpdateMessage(@NonNull Recipient recipient,
                                    @NonNull GroupContext group,
                                    @Nullable final Attachment avatar,
                                    long sentTimeMillis,
                                    long expireIn,
                                    boolean viewOnce,
                                    @Nullable QuoteModel quote,
                                    @NonNull List<Contact> contacts,
                                    @NonNull List<LinkPreview> previews)
  {
    this(recipient, new MessageGroupContext(group), getAttachments(avatar), sentTimeMillis, expireIn, viewOnce, quote, contacts, previews);
  }

  public OutgoingGroupUpdateMessage(@NonNull Recipient recipient,
                                    @NonNull DecryptedGroupV2Context group,
                                    @Nullable final Attachment avatar,
                                    long sentTimeMillis,
                                    long expireIn,
                                    boolean viewOnce,
                                    @Nullable QuoteModel quote,
                                    @NonNull List<Contact> contacts,
                                    @NonNull List<LinkPreview> previews)
  {
    this(recipient, new MessageGroupContext(group), getAttachments(avatar), sentTimeMillis, expireIn, viewOnce, quote, contacts, previews);
  }

  @Override
  public boolean isGroup() {
    return true;
  }

  public boolean isV2Group() {
    return messageGroupContext.isV2Group();
  }

  public @NonNull MessageGroupContext.GroupV1Properties requireGroupV1Properties() {
    return messageGroupContext.requireGroupV1Properties();
  }

  public @NonNull MessageGroupContext.GroupV2Properties requireGroupV2Properties() {
    return messageGroupContext.requireGroupV2Properties();
  }

  private static List<Attachment> getAttachments(@Nullable Attachment avatar) {
    return avatar == null ? Collections.emptyList() : Collections.singletonList(avatar);
  }
}
