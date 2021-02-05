package com.zamzam.chat.stickers;

import android.database.Cursor;
import androidx.annotation.NonNull;

import com.zamzam.chat.database.StickerDatabase;
import com.zamzam.chat.database.StickerDatabase.StickerPackRecordReader;
import com.zamzam.chat.database.StickerDatabase.StickerRecordReader;
import com.zamzam.chat.database.model.StickerPackRecord;
import com.zamzam.chat.database.model.StickerRecord;
import com.zamzam.chat.util.concurrent.SignalExecutors;

import java.util.ArrayList;
import java.util.List;

final class StickerKeyboardRepository {

  private static final int RECENT_LIMIT = 24;

  private final StickerDatabase stickerDatabase;

  StickerKeyboardRepository(@NonNull StickerDatabase stickerDatabase) {
    this.stickerDatabase = stickerDatabase;
  }

  void getPackList(@NonNull Callback<PackListResult> callback) {
    SignalExecutors.BOUNDED.execute(() -> {
      List<StickerPackRecord> packs = new ArrayList<>();

      try (StickerPackRecordReader reader = new StickerPackRecordReader(stickerDatabase.getInstalledStickerPacks())) {
        StickerPackRecord pack;
        while ((pack = reader.getNext()) != null) {
          packs.add(pack);
        }
      }

      boolean hasRecents;

      try (Cursor recentsCursor = stickerDatabase.getRecentlyUsedStickers(1)) {
        hasRecents = recentsCursor != null && recentsCursor.moveToFirst();
      }

      callback.onComplete(new PackListResult(packs, hasRecents));
    });
  }

  void getStickersForPack(@NonNull String packId, @NonNull Callback<List<StickerRecord>> callback) {
    SignalExecutors.BOUNDED.execute(() -> {
      List<StickerRecord> stickers = new ArrayList<>();

      try (StickerRecordReader reader = new StickerRecordReader(stickerDatabase.getStickersForPack(packId))) {
        StickerRecord sticker;
        while ((sticker = reader.getNext()) != null) {
          stickers.add(sticker);
        }
      }

      callback.onComplete(stickers);
    });
  }

  void getRecentStickers(@NonNull Callback<List<StickerRecord>> callback) {
    SignalExecutors.BOUNDED.execute(() -> {
      List<StickerRecord> stickers = new ArrayList<>();

      try (StickerRecordReader reader = new StickerRecordReader(stickerDatabase.getRecentlyUsedStickers(RECENT_LIMIT))) {
        StickerRecord sticker;
        while ((sticker = reader.getNext()) != null) {
          stickers.add(sticker);
        }
      }

      callback.onComplete(stickers);
    });
  }

  static class PackListResult {

    private final List<StickerPackRecord> packs;
    private final boolean                 hasRecents;

    PackListResult(List<StickerPackRecord> packs, boolean hasRecents) {
      this.packs      = packs;
      this.hasRecents = hasRecents;
    }

    List<StickerPackRecord> getPacks() {
      return packs;
    }

    boolean hasRecents() {
      return hasRecents;
    }
  }

  interface Callback<T> {
    void onComplete(T result);
  }
}
