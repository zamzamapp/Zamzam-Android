package com.zamzam.chat.database.loaders;

import android.content.Context;

import com.zamzam.chat.util.AbstractCursorLoader;

public abstract class MediaLoader extends AbstractCursorLoader {

  MediaLoader(Context context) {
    super(context);
  }

  public enum MediaType {
    GALLERY,
    DOCUMENT,
    AUDIO,
    ALL
  }
}
