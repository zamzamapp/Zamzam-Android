package com.zamzam.chat.giph.net;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GiphyStickerLoader extends GiphyLoader {

  public GiphyStickerLoader(@NonNull Context context, @Nullable String searchString) {
    super(context, searchString);
  }

  @Override
  protected String getTrendingUrl() {
    return "https://api.giphy.com/v1/stickers/trending?api_key=BDM2OCeVXXoylNTNgQ6i8GEreNq6HHKJ&offset=%d&limit=" + PAGE_SIZE;
  }

  @Override
  protected String getSearchUrl() {
    return "https://api.giphy.com/v1/stickers/search?api_key=BDM2OCeVXXoylNTNgQ6i8GEreNq6HHKJ&offset=%d&limit=" + PAGE_SIZE + "&q=%s";
  }
}
