package com.zamzam.chat.database.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.zamzam.chat.attachments.AttachmentId;
import com.zamzam.chat.database.AttachmentDatabase;
import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.database.MediaDatabase;
import com.zamzam.chat.database.MediaDatabase.Sorting;
import com.zamzam.chat.mms.PartAuthority;
import com.zamzam.chat.util.AsyncLoader;

public final class PagingMediaLoader extends AsyncLoader<Pair<Cursor, Integer>> {

  @SuppressWarnings("unused")
  private static final String TAG = PagingMediaLoader.class.getSimpleName();

  private final Uri     uri;
  private final boolean leftIsRecent;
  private final Sorting sorting;
  private final long    threadId;

  public PagingMediaLoader(@NonNull Context context, long threadId, @NonNull Uri uri, boolean leftIsRecent, @NonNull Sorting sorting) {
    super(context);
    this.threadId     = threadId;
    this.uri          = uri;
    this.leftIsRecent = leftIsRecent;
    this.sorting      = sorting;
  }

  @Override
  public @Nullable Pair<Cursor, Integer> loadInBackground() {
    Cursor cursor = DatabaseFactory.getMediaDatabase(getContext()).getGalleryMediaForThread(threadId, sorting, threadId == MediaDatabase.ALL_THREADS);

    while (cursor.moveToNext()) {
      AttachmentId attachmentId  = new AttachmentId(cursor.getLong(cursor.getColumnIndexOrThrow(AttachmentDatabase.ROW_ID)), cursor.getLong(cursor.getColumnIndexOrThrow(AttachmentDatabase.UNIQUE_ID)));
      Uri          attachmentUri = PartAuthority.getAttachmentDataUri(attachmentId);

      if (attachmentUri.equals(uri)) {
        return new Pair<>(cursor, leftIsRecent ? cursor.getPosition() : cursor.getCount() - 1 - cursor.getPosition());
      }
    }

    return null;
  }
}
