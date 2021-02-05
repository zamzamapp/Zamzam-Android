package com.zamzam.chat.conversationlist;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import com.zamzam.chat.conversationlist.model.Conversation;
import com.zamzam.chat.database.DatabaseContentProviders;
import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.database.ThreadDatabase;
import com.zamzam.chat.database.model.ThreadRecord;
import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.util.ThrottledDebouncer;
import com.zamzam.chat.util.concurrent.SignalExecutors;
import com.zamzam.chat.util.paging.Invalidator;
import com.zamzam.chat.util.paging.SizeFixResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

abstract class ConversationListDataSource extends PositionalDataSource<Conversation> {

  public static final Executor EXECUTOR = SignalExecutors.newFixedLifoThreadExecutor("signal-conversation-list", 1, 1);

  private static final ThrottledDebouncer THROTTLER = new ThrottledDebouncer(500);

  private static final String TAG = Log.tag(ConversationListDataSource.class);

  protected final ThreadDatabase threadDatabase;

  protected ConversationListDataSource(@NonNull Context context, @NonNull Invalidator invalidator) {
    this.threadDatabase = DatabaseFactory.getThreadDatabase(context);

    ContentObserver contentObserver = new ContentObserver(null) {
      @Override
      public void onChange(boolean selfChange) {
        THROTTLER.publish(() -> {
          invalidate();
          context.getContentResolver().unregisterContentObserver(this);
        });
      }
    };

    invalidator.observe(() -> {
      invalidate();
      context.getContentResolver().unregisterContentObserver(contentObserver);
    });

    context.getContentResolver().registerContentObserver(DatabaseContentProviders.ConversationList.CONTENT_URI,  true, contentObserver);
  }

  private static ConversationListDataSource create(@NonNull Context context, @NonNull Invalidator invalidator, boolean isArchived) {
    if (!isArchived) return new UnarchivedConversationListDataSource(context, invalidator);
    else             return new ArchivedConversationListDataSource(context, invalidator);
  }

  @Override
  public final void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<Conversation> callback) {
    long start = System.currentTimeMillis();

    List<Conversation> conversations  = new ArrayList<>(params.requestedLoadSize);
    int                totalCount     = getTotalCount();
    int                effectiveCount = params.requestedStartPosition;
    List<Recipient>    recipients     = new LinkedList<>();

    try (ThreadDatabase.Reader reader = threadDatabase.readerFor(getCursor(params.requestedStartPosition, params.requestedLoadSize))) {
      ThreadRecord record;
      while ((record = reader.getNext()) != null && effectiveCount < totalCount && !isInvalid()) {
        conversations.add(new Conversation(record));
        recipients.add(record.getRecipient());
        effectiveCount++;
      }
    }

    ApplicationDependencies.getRecipientCache().addToCache(recipients);

    if (!isInvalid()) {
      SizeFixResult<Conversation> result = SizeFixResult.ensureMultipleOfPageSize(conversations, params.requestedStartPosition, params.pageSize, totalCount);
      callback.onResult(result.getItems(), params.requestedStartPosition, result.getTotal());
      Log.d(TAG, "[Initial Load] " + (System.currentTimeMillis() - start) + " ms | start: " + params.requestedStartPosition + ", requestedSize: " + params.requestedLoadSize + ", actualSize: " + result.getItems().size() + ", totalCount: " + result.getTotal() + ", class: " + getClass().getSimpleName());
    } else {
      Log.d(TAG, "[Initial Load] " + (System.currentTimeMillis() - start) + " ms | start: " + params.requestedStartPosition + ", requestedSize: " + params.requestedLoadSize + ", totalCount: " + totalCount + ", class: " + getClass().getSimpleName() + " -- invalidated");
    }
  }

  @Override
  public final void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<Conversation> callback) {
    long start = System.currentTimeMillis();

    List<Conversation> conversations = new ArrayList<>(params.loadSize);
    List<Recipient>    recipients    = new LinkedList<>();

    try (ThreadDatabase.Reader reader = threadDatabase.readerFor(getCursor(params.startPosition, params.loadSize))) {
      ThreadRecord record;
      while ((record = reader.getNext()) != null && !isInvalid()) {
        conversations.add(new Conversation(record));
        recipients.add(record.getRecipient());
      }
    }

    ApplicationDependencies.getRecipientCache().addToCache(recipients);

    callback.onResult(conversations);

    Log.d(TAG, "[Update] " + (System.currentTimeMillis() - start) + " ms | start: " + params.startPosition + ", size: " + params.loadSize + ", class: " + getClass().getSimpleName() + (isInvalid() ? " -- invalidated" : ""));
  }

  protected abstract int getTotalCount();
  protected abstract Cursor getCursor(long offset, long limit);

  private static class ArchivedConversationListDataSource extends ConversationListDataSource {

    ArchivedConversationListDataSource(@NonNull Context context, @NonNull Invalidator invalidator) {
      super(context, invalidator);
    }

    @Override
    protected int getTotalCount() {
      return threadDatabase.getArchivedConversationListCount();
    }

    @Override
    protected Cursor getCursor(long offset, long limit) {
      return threadDatabase.getArchivedConversationList(offset, limit);
    }
  }

  private static class UnarchivedConversationListDataSource extends ConversationListDataSource {

    UnarchivedConversationListDataSource(@NonNull Context context, @NonNull Invalidator invalidator) {
      super(context, invalidator);
    }

    @Override
    protected int getTotalCount() {
      return threadDatabase.getUnarchivedConversationListCount();
    }

    @Override
    protected Cursor getCursor(long offset, long limit) {
      return threadDatabase.getConversationList(offset, limit);
    }
  }

  static class Factory extends DataSource.Factory<Integer, Conversation> {

    private final Context     context;
    private final Invalidator invalidator;
    private final boolean     isArchived;

    public Factory(@NonNull Context context, @NonNull Invalidator invalidator, boolean isArchived) {
      this.context     = context;
      this.invalidator = invalidator;
      this.isArchived  = isArchived;
    }

    @Override
    public @NonNull DataSource<Integer, Conversation> create() {
      return ConversationListDataSource.create(context, invalidator, isArchived);
    }
  }
}
