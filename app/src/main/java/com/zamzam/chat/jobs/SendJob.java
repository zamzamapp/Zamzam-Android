package com.zamzam.chat.jobs;

import androidx.annotation.NonNull;

import com.zamzam.chat.BuildConfig;
import com.zamzam.chat.TextSecureExpiredException;
import com.zamzam.chat.attachments.Attachment;
import com.zamzam.chat.database.AttachmentDatabase;
import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.jobmanager.Job;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.util.Util;

import java.util.List;

public abstract class SendJob extends BaseJob {

  @SuppressWarnings("unused")
  private final static String TAG = SendJob.class.getSimpleName();

  public SendJob(Job.Parameters parameters) {
    super(parameters);
  }

  @Override
  public final void onRun() throws Exception {
    if (Util.getDaysTillBuildExpiry() <= 0) {
      throw new TextSecureExpiredException(String.format("TextSecure expired (build %d, now %d)",
                                                         BuildConfig.BUILD_TIMESTAMP,
                                                         System.currentTimeMillis()));
    }

    Log.i(TAG, "Starting message send attempt");
    onSend();
    Log.i(TAG, "Message send completed");
  }

  protected abstract void onSend() throws Exception;

  protected void markAttachmentsUploaded(long messageId, @NonNull List<Attachment> attachments) {
    AttachmentDatabase database = DatabaseFactory.getAttachmentDatabase(context);

    for (Attachment attachment : attachments) {
      database.markAttachmentUploaded(messageId, attachment);
    }
  }
}
