package com.zamzam.chat.jobs;


import androidx.annotation.NonNull;

import com.zamzam.chat.crypto.UnidentifiedAccessUtil;
import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.jobmanager.Data;
import com.zamzam.chat.jobmanager.Job;
import com.zamzam.chat.jobmanager.impl.NetworkConstraint;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.recipients.RecipientId;
import com.zamzam.chat.recipients.RecipientUtil;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.messages.SignalServiceReceiptMessage;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SendDeliveryReceiptJob extends BaseJob {

  public static final String KEY = "SendDeliveryReceiptJob";

  private static final String KEY_RECIPIENT  = "recipient";
  private static final String KEY_MESSAGE_ID = "message_id";
  private static final String KEY_TIMESTAMP  = "timestamp";

  private static final String TAG = SendReadReceiptJob.class.getSimpleName();

  private RecipientId recipientId;
  private long        messageId;
  private long        timestamp;

  public SendDeliveryReceiptJob(@NonNull RecipientId recipientId, long messageId) {
    this(new Job.Parameters.Builder()
                           .addConstraint(NetworkConstraint.KEY)
                           .setLifespan(TimeUnit.DAYS.toMillis(1))
                           .setMaxAttempts(Parameters.UNLIMITED)
                           .build(),
         recipientId,
         messageId,
         System.currentTimeMillis());
  }

  private SendDeliveryReceiptJob(@NonNull Job.Parameters parameters,
                                 @NonNull RecipientId recipientId,
                                 long messageId,
                                 long timestamp)
  {
    super(parameters);

    this.recipientId = recipientId;
    this.messageId   = messageId;
    this.timestamp   = timestamp;
  }

  @Override
  public @NonNull Data serialize() {
    return new Data.Builder().putString(KEY_RECIPIENT, recipientId.serialize())
                             .putLong(KEY_MESSAGE_ID, messageId)
                             .putLong(KEY_TIMESTAMP, timestamp)
                             .build();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onRun() throws IOException, UntrustedIdentityException {
    SignalServiceMessageSender  messageSender  = ApplicationDependencies.getSignalServiceMessageSender();
    Recipient                   recipient      = Recipient.resolved(recipientId);
    SignalServiceAddress        remoteAddress  = RecipientUtil.toSignalServiceAddress(context, recipient);
    SignalServiceReceiptMessage receiptMessage = new SignalServiceReceiptMessage(SignalServiceReceiptMessage.Type.DELIVERY,
                                                                                 Collections.singletonList(messageId),
                                                                                 timestamp);

    messageSender.sendReceipt(remoteAddress,
                              UnidentifiedAccessUtil.getAccessFor(context, recipient),
                              receiptMessage);
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception e) {
    if (e instanceof PushNetworkException) return true;
    return false;
  }

  @Override
  public void onFailure() {
    Log.w(TAG, "Failed to send delivery receipt to: " + recipientId);
  }

  public static final class Factory implements Job.Factory<SendDeliveryReceiptJob> {
    @Override
    public @NonNull SendDeliveryReceiptJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new SendDeliveryReceiptJob(parameters,
                                        RecipientId.from(data.getString(KEY_RECIPIENT)),
                                        data.getLong(KEY_MESSAGE_ID),
                                        data.getLong(KEY_TIMESTAMP));
    }
  }
}
