package com.zamzam.chat.attachments;

import android.net.Uri;

import androidx.annotation.Nullable;

import com.zamzam.chat.audio.AudioHash;
import com.zamzam.chat.blurhash.BlurHash;
import com.zamzam.chat.database.AttachmentDatabase.TransformProperties;
import com.zamzam.chat.mms.PartAuthority;
import com.zamzam.chat.stickers.StickerLocator;

import java.util.Comparator;

public class DatabaseAttachment extends Attachment {

  private final AttachmentId attachmentId;
  private final long         mmsId;
  private final boolean      hasData;
  private final boolean      hasThumbnail;
  private final int          displayOrder;

  public DatabaseAttachment(AttachmentId attachmentId,
                            long mmsId,
                            boolean hasData,
                            boolean hasThumbnail,
                            String contentType,
                            int transferProgress,
                            long size,
                            String fileName,
                            int cdnNumber,
                            String location,
                            String key,
                            String relay,
                            byte[] digest,
                            String fastPreflightId,
                            boolean voiceNote,
                            boolean borderless,
                            int width,
                            int height,
                            boolean quote,
                            @Nullable String caption,
                            @Nullable StickerLocator stickerLocator,
                            @Nullable BlurHash blurHash,
                            @Nullable AudioHash audioHash,
                            @Nullable TransformProperties transformProperties,
                            int displayOrder,
                            long uploadTimestamp)
  {
    super(contentType, transferProgress, size, fileName, cdnNumber, location, key, relay, digest, fastPreflightId, voiceNote, borderless, width, height, quote, uploadTimestamp, caption, stickerLocator, blurHash, audioHash, transformProperties);
    this.attachmentId = attachmentId;
    this.hasData      = hasData;
    this.hasThumbnail = hasThumbnail;
    this.mmsId        = mmsId;
    this.displayOrder = displayOrder;
  }

  @Override
  @Nullable
  public Uri getDataUri() {
    if (hasData) {
      return PartAuthority.getAttachmentDataUri(attachmentId);
    } else {
      return null;
    }
  }

  @Override
  @Nullable
  public Uri getThumbnailUri() {
    if (hasThumbnail) {
      return PartAuthority.getAttachmentThumbnailUri(attachmentId);
    } else {
      return null;
    }
  }

  public AttachmentId getAttachmentId() {
    return attachmentId;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  @Override
  public boolean equals(Object other) {
    return other != null &&
           other instanceof DatabaseAttachment &&
           ((DatabaseAttachment) other).attachmentId.equals(this.attachmentId);
  }

  @Override
  public int hashCode() {
    return attachmentId.hashCode();
  }

  public long getMmsId() {
    return mmsId;
  }

  public boolean hasData() {
    return hasData;
  }

  public boolean hasThumbnail() {
    return hasThumbnail;
  }

  public static class DisplayOrderComparator implements Comparator<DatabaseAttachment> {
    @Override
    public int compare(DatabaseAttachment lhs, DatabaseAttachment rhs) {
      return Integer.compare(lhs.getDisplayOrder(), rhs.getDisplayOrder());
    }
  }
}
