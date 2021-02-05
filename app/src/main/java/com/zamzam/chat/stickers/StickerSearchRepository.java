package com.zamzam.chat.stickers;

import android.content.Context;
import android.database.Cursor;
import androidx.annotation.NonNull;

import com.zamzam.chat.components.emoji.EmojiUtil;
import com.zamzam.chat.database.AttachmentDatabase;
import com.zamzam.chat.database.CursorList;
import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.database.StickerDatabase;
import com.zamzam.chat.database.StickerDatabase.StickerRecordReader;
import com.zamzam.chat.database.model.StickerRecord;
import com.zamzam.chat.util.concurrent.SignalExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class StickerSearchRepository {

  private final StickerDatabase    stickerDatabase;
  private final AttachmentDatabase attachmentDatabase;

  public StickerSearchRepository(@NonNull Context context) {
    this.stickerDatabase    = DatabaseFactory.getStickerDatabase(context);
    this.attachmentDatabase = DatabaseFactory.getAttachmentDatabase(context);
  }

  public void searchByEmoji(@NonNull String emoji, @NonNull Callback<List<StickerRecord>> callback) {
    SignalExecutors.BOUNDED.execute(() -> {
      String              searchEmoji = EmojiUtil.getCanonicalRepresentation(emoji);
      List<StickerRecord> out         = new ArrayList<>();
      Set<String>         possible    = EmojiUtil.getAllRepresentations(searchEmoji);

      for (String candidate : possible) {
        try (StickerRecordReader reader = new StickerRecordReader(stickerDatabase.getStickersByEmoji(candidate))) {
          StickerRecord record = null;
          while ((record = reader.getNext()) != null) {
            out.add(record);
          }
        }
      }

      callback.onResult(out);
    });
  }

  public void getStickerFeatureAvailability(@NonNull Callback<Boolean> callback) {
    SignalExecutors.BOUNDED.execute(() -> {
      try (Cursor cursor = stickerDatabase.getAllStickerPacks("1")) {
        if (cursor != null && cursor.moveToFirst()) {
          callback.onResult(true);
        } else {
          callback.onResult(attachmentDatabase.hasStickerAttachments());
        }
      }
    });
  }

  private static class StickerModelBuilder implements CursorList.ModelBuilder<StickerRecord> {
    @Override
    public StickerRecord build(@NonNull Cursor cursor) {
      return new StickerRecordReader(cursor).getCurrent();
    }
  }

  public interface Callback<T> {
    void onResult(T result);
  }
}
