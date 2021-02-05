package com.zamzam.chat.database.loaders;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.database.MediaDatabase;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.recipients.RecipientId;

/**
 * It is more efficient to use the {@link ThreadMediaLoader} if you know the thread id already.
 */
public final class RecipientMediaLoader extends MediaLoader {

  @Nullable private final RecipientId           recipientId;
  @NonNull  private final MediaType             mediaType;
  @NonNull  private final MediaDatabase.Sorting sorting;

  public RecipientMediaLoader(@NonNull Context context,
                              @Nullable RecipientId recipientId,
                              @NonNull MediaType mediaType,
                              @NonNull MediaDatabase.Sorting sorting)
  {
    super(context);
    this.recipientId = recipientId;
    this.mediaType   = mediaType;
    this.sorting     = sorting;
  }

  @Override
  public Cursor getCursor() {
    if (recipientId == null || recipientId.isUnknown()) return null;

    long threadId = DatabaseFactory.getThreadDatabase(getContext())
                                   .getThreadIdFor(Recipient.resolved(recipientId));

    return ThreadMediaLoader.createThreadMediaCursor(context, threadId, mediaType, sorting);
  }

}
