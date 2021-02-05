package com.zamzam.chat.mms;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.Nullable;

import com.zamzam.chat.attachments.Attachment;
import com.zamzam.chat.util.MediaUtil;

public class GifSlide extends ImageSlide {

  private final boolean borderless;

  public GifSlide(Context context, Attachment attachment) {
    super(context, attachment);
    this.borderless = attachment.isBorderless();
  }

  public GifSlide(Context context, Uri uri, long size, int width, int height) {
    this(context, uri, size, width, height, false, null);
  }

  public GifSlide(Context context, Uri uri, long size, int width, int height, boolean borderless, @Nullable String caption) {
    super(context, constructAttachmentFromUri(context, uri, MediaUtil.IMAGE_GIF, size, width, height, true, null, caption, null, null, null, false, borderless, false));
    this.borderless = borderless;
  }

  @Override
  public @Nullable Uri getThumbnailUri() {
    return getUri();
  }

  @Override
  public boolean isBorderless() {
    return borderless;
  }
}
