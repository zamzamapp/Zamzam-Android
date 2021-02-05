package com.zamzam.chat.glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import com.zamzam.chat.giph.model.ChunkedImageUrl;
import com.zamzam.chat.net.ContentProxySafetyInterceptor;
import com.zamzam.chat.net.ContentProxySelector;
import com.zamzam.chat.net.UserAgentInterceptor;
import com.zamzam.chat.push.SignalServiceNetworkAccess;

import java.io.InputStream;

import okhttp3.OkHttpClient;

public class ChunkedImageUrlLoader implements ModelLoader<ChunkedImageUrl, InputStream> {

  private final OkHttpClient client;

  private ChunkedImageUrlLoader(OkHttpClient client) {
    this.client  = client;
  }

  @Override
  public @Nullable LoadData<InputStream> buildLoadData(@NonNull ChunkedImageUrl url, int width, int height, @NonNull Options options) {
    return new LoadData<>(url, new ChunkedImageUrlFetcher(client, url));
  }

  @Override
  public boolean handles(@NonNull ChunkedImageUrl url) {
    return true;
  }

  public static class Factory implements ModelLoaderFactory<ChunkedImageUrl, InputStream> {

    private final OkHttpClient client;

    public Factory() {
      this.client  = new OkHttpClient.Builder()
                                     .proxySelector(new ContentProxySelector())
                                     .cache(null)
                                     .addInterceptor(new UserAgentInterceptor())
                                     .addNetworkInterceptor(new ContentProxySafetyInterceptor())
                                     .addNetworkInterceptor(new PaddedHeadersInterceptor())
                                     .dns(SignalServiceNetworkAccess.DNS)
                                     .build();
    }

    @Override
    public @NonNull ModelLoader<ChunkedImageUrl, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
      return new ChunkedImageUrlLoader(client);
    }

    @Override
    public void teardown() {}
  }
}