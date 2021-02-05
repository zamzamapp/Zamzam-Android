package com.zamzam.chat.conversation.ui.mentions;

import androidx.annotation.Nullable;

import com.zamzam.chat.conversation.ui.mentions.MentionViewHolder.MentionEventsListener;
import com.zamzam.chat.util.MappingAdapter;

public class MentionsPickerAdapter extends MappingAdapter {
  public MentionsPickerAdapter(@Nullable MentionEventsListener mentionEventsListener) {
    registerFactory(MentionViewState.class, MentionViewHolder.createFactory(mentionEventsListener));
  }
}
