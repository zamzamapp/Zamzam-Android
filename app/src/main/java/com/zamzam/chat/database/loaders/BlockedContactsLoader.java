package com.zamzam.chat.database.loaders;

import android.content.Context;
import android.database.Cursor;

import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.util.AbstractCursorLoader;

public class BlockedContactsLoader extends AbstractCursorLoader {

  public BlockedContactsLoader(Context context) {
    super(context);
  }

  @Override
  public Cursor getCursor() {
    return DatabaseFactory.getRecipientDatabase(getContext()).getBlocked();
  }

}
