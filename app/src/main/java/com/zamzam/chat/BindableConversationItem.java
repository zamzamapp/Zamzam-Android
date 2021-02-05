package com.zamzam.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.zamzam.chat.contactshare.Contact;
import com.zamzam.chat.conversation.ConversationMessage;
import com.zamzam.chat.database.model.MessageRecord;
import com.zamzam.chat.database.model.MmsMessageRecord;
import com.zamzam.chat.groups.GroupId;
import com.zamzam.chat.linkpreview.LinkPreview;
import com.zamzam.chat.mms.GlideRequests;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.recipients.RecipientId;
import com.zamzam.chat.stickers.StickerLocator;
import org.whispersystems.libsignal.util.guava.Optional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface BindableConversationItem extends Unbindable {
  void bind(@NonNull ConversationMessage messageRecord,
            @NonNull Optional<MessageRecord> previousMessageRecord,
            @NonNull Optional<MessageRecord> nextMessageRecord,
            @NonNull GlideRequests glideRequests,
            @NonNull Locale locale,
            @NonNull Set<ConversationMessage> batchSelected,
            @NonNull Recipient recipients,
            @Nullable String searchQuery,
            boolean pulseHighlight);

  ConversationMessage getConversationMessage();

  void setEventListener(@Nullable EventListener listener);

  interface EventListener {
    void onQuoteClicked(MmsMessageRecord messageRecord);
    void onLinkPreviewClicked(@NonNull LinkPreview linkPreview);
    void onMoreTextClicked(@NonNull RecipientId conversationRecipientId, long messageId, boolean isMms);
    void onStickerClicked(@NonNull StickerLocator stickerLocator);
    void onViewOnceMessageClicked(@NonNull MmsMessageRecord messageRecord);
    void onSharedContactDetailsClicked(@NonNull Contact contact, @NonNull View avatarTransitionView);
    void onAddToContactsClicked(@NonNull Contact contact);
    void onMessageSharedContactClicked(@NonNull List<Recipient> choices);
    void onInviteSharedContactClicked(@NonNull List<Recipient> choices);
    void onReactionClicked(@NonNull View reactionTarget, long messageId, boolean isMms);
    void onGroupMemberAvatarClicked(@NonNull RecipientId recipientId, @NonNull GroupId groupId);
    void onMessageWithErrorClicked(@NonNull MessageRecord messageRecord);
  }
}
