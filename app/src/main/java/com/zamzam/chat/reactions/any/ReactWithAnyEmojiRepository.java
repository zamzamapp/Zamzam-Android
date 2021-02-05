package com.zamzam.chat.reactions.any;

import android.content.Context;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.annimon.stream.Stream;

import com.zamzam.chat.R;
import com.zamzam.chat.components.emoji.EmojiUtil;
import com.zamzam.chat.components.emoji.RecentEmojiPageModel;
import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.database.MessagingDatabase;
import com.zamzam.chat.database.NoSuchMessageException;
import com.zamzam.chat.database.model.MessageRecord;
import com.zamzam.chat.database.model.ReactionRecord;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.reactions.ReactionDetails;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.sms.MessageSender;
import com.zamzam.chat.util.Util;
import com.zamzam.chat.util.concurrent.SignalExecutors;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

final class ReactWithAnyEmojiRepository {

  private static final String TAG = Log.tag(ReactWithAnyEmojiRepository.class);

  private static final String RECENT_STORAGE_KEY = "reactions_recent_emoji";

  private final Context              context;
  private final RecentEmojiPageModel recentEmojiPageModel;
  private final List<ReactWithAnyEmojiPage> emojiPages;

  ReactWithAnyEmojiRepository(@NonNull Context context) {
    this.context              = context;
    this.recentEmojiPageModel = new RecentEmojiPageModel(context, RECENT_STORAGE_KEY);
    this.emojiPages           = new LinkedList<>();

    emojiPages.addAll(Stream.of(EmojiUtil.getDisplayPages())
                            .map(page -> new ReactWithAnyEmojiPage(Collections.singletonList(new ReactWithAnyEmojiPageBlock(getCategoryLabel(page.getIconAttr()), page))))
                            .toList());
    emojiPages.remove(emojiPages.size() - 1);
  }

  List<ReactWithAnyEmojiPage> getEmojiPageModels(@NonNull List<ReactionDetails> thisMessagesReactions) {
    List<ReactWithAnyEmojiPage> pages       = new LinkedList<>();
    List<String>                thisMessage = Stream.of(thisMessagesReactions)
                                                    .map(ReactionDetails::getDisplayEmoji)
                                                    .distinct()
                                                    .toList();

    if (thisMessage.isEmpty()) {
      pages.add(new ReactWithAnyEmojiPage(Collections.singletonList(new ReactWithAnyEmojiPageBlock(R.string.ReactWithAnyEmojiBottomSheetDialogFragment__recently_used, recentEmojiPageModel))));
    } else {
      pages.add(new ReactWithAnyEmojiPage(Arrays.asList(new ReactWithAnyEmojiPageBlock(R.string.ReactWithAnyEmojiBottomSheetDialogFragment__this_message, new ThisMessageEmojiPageModel(thisMessage)),
                                                        new ReactWithAnyEmojiPageBlock(R.string.ReactWithAnyEmojiBottomSheetDialogFragment__recently_used, recentEmojiPageModel))));
    }

    pages.addAll(emojiPages);

    return pages;
  }

  void addEmojiToMessage(@NonNull String emoji, long messageId, boolean isMms) {
    SignalExecutors.BOUNDED.execute(() -> {
      try {
        MessagingDatabase db            = isMms ? DatabaseFactory.getMmsDatabase(context) : DatabaseFactory.getSmsDatabase(context);
        MessageRecord     messageRecord = db.getMessageRecord(messageId);
        ReactionRecord    oldRecord     = Stream.of(messageRecord.getReactions())
                                                .filter(record -> record.getAuthor().equals(Recipient.self().getId()))
                                                .findFirst()
                                                .orElse(null);

        if (oldRecord != null && oldRecord.getEmoji().equals(emoji)) {
          MessageSender.sendReactionRemoval(context, messageRecord.getId(), messageRecord.isMms(), oldRecord);
        } else {
          MessageSender.sendNewReaction(context, messageRecord.getId(), messageRecord.isMms(), emoji);
          Util.runOnMain(() -> recentEmojiPageModel.onCodePointSelected(emoji));
        }
      } catch (NoSuchMessageException e) {
        Log.w(TAG, "Message not found! Ignoring.");
      }
    });
  }

  private @StringRes int getCategoryLabel(@AttrRes int iconAttr) {
    switch (iconAttr) {
      case R.attr.emoji_category_people:
        return R.string.ReactWithAnyEmojiBottomSheetDialogFragment__smileys_and_people;
      case R.attr.emoji_category_nature:
        return R.string.ReactWithAnyEmojiBottomSheetDialogFragment__nature;
      case R.attr.emoji_category_foods:
        return R.string.ReactWithAnyEmojiBottomSheetDialogFragment__food;
      case R.attr.emoji_category_activity:
        return R.string.ReactWithAnyEmojiBottomSheetDialogFragment__activities;
      case R.attr.emoji_category_places:
        return R.string.ReactWithAnyEmojiBottomSheetDialogFragment__places;
      case R.attr.emoji_category_objects:
        return R.string.ReactWithAnyEmojiBottomSheetDialogFragment__objects;
      case R.attr.emoji_category_symbols:
        return R.string.ReactWithAnyEmojiBottomSheetDialogFragment__symbols;
      case R.attr.emoji_category_flags:
        return R.string.ReactWithAnyEmojiBottomSheetDialogFragment__flags;
      case R.attr.emoji_category_emoticons:
        return R.string.ReactWithAnyEmojiBottomSheetDialogFragment__emoticons;
      default:
        throw new AssertionError();
    }
  }
}
