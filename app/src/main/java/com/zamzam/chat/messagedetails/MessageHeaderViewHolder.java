package com.zamzam.chat.messagedetails;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zamzam.chat.R;
import com.zamzam.chat.conversation.ConversationItem;
import com.zamzam.chat.conversation.ConversationMessage;
import com.zamzam.chat.database.model.MessageRecord;
import com.zamzam.chat.mms.GlideRequests;
import com.zamzam.chat.sms.MessageSender;
import com.zamzam.chat.util.DateUtils;
import com.zamzam.chat.util.ExpirationUtil;
import com.zamzam.chat.util.Util;
import com.zamzam.chat.util.concurrent.SignalExecutors;
import org.whispersystems.libsignal.util.guava.Optional;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;

final class MessageHeaderViewHolder extends RecyclerView.ViewHolder {
  private final TextView         sentDate;
  private final TextView         receivedDate;
  private final TextView         expiresIn;
  private final TextView         transport;
  private final View             expiresGroup;
  private final View             receivedGroup;
  private final TextView         errorText;
  private final View             resendButton;
  private final View             messageMetadata;
  private final ViewStub         updateStub;
  private final ViewStub         sentStub;
  private final ViewStub         receivedStub;

  private       GlideRequests    glideRequests;
  private       ConversationItem conversationItem;
  private       ExpiresUpdater   expiresUpdater;

  MessageHeaderViewHolder(@NonNull View itemView, GlideRequests glideRequests) {
    super(itemView);
    this.glideRequests = glideRequests;

    sentDate        = itemView.findViewById(R.id.message_details_header_sent_time);
    receivedDate    = itemView.findViewById(R.id.message_details_header_received_time);
    receivedGroup   = itemView.findViewById(R.id.message_details_header_received_group);
    expiresIn       = itemView.findViewById(R.id.message_details_header_expires_in);
    expiresGroup    = itemView.findViewById(R.id.message_details_header_expires_group);
    transport       = itemView.findViewById(R.id.message_details_header_transport);
    errorText       = itemView.findViewById(R.id.message_details_header_error_text);
    resendButton    = itemView.findViewById(R.id.message_details_header_resend_button);
    messageMetadata = itemView.findViewById(R.id.message_details_header_message_metadata);
    updateStub      = itemView.findViewById(R.id.message_details_header_message_view_update);
    sentStub        = itemView.findViewById(R.id.message_details_header_message_view_sent_multimedia);
    receivedStub    = itemView.findViewById(R.id.message_details_header_message_view_received_multimedia);
  }

  void bind(MessageRecord messageRecord, boolean running) {
    bindMessageView(messageRecord);
    bindErrorState(messageRecord);
    bindSentReceivedDates(messageRecord);
    bindExpirationTime(messageRecord, running);
    bindTransport(messageRecord);
  }

  void partialBind(MessageRecord messageRecord, boolean running) {
    bindExpirationTime(messageRecord, running);
  }

  private void bindMessageView(MessageRecord messageRecord) {
    if (conversationItem == null) {
      if      (messageRecord.isGroupAction()) conversationItem = (ConversationItem) updateStub.inflate();
      else if (messageRecord.isOutgoing())    conversationItem = (ConversationItem) sentStub.inflate();
      else                                    conversationItem = (ConversationItem) receivedStub.inflate();
    }
    conversationItem.bind(new ConversationMessage(messageRecord), Optional.absent(), Optional.absent(), glideRequests, Locale.getDefault(), new HashSet<>(), messageRecord.getRecipient(), null, false);
  }

  private void bindErrorState(MessageRecord messageRecord) {
    if (messageRecord.hasFailedWithNetworkFailures()) {
      errorText.setVisibility(View.VISIBLE);
      resendButton.setVisibility(View.VISIBLE);
      resendButton.setOnClickListener(unused -> {
        resendButton.setOnClickListener(null);
        SignalExecutors.BOUNDED.execute(() -> MessageSender.resend(itemView.getContext().getApplicationContext(), messageRecord));
      });
      messageMetadata.setVisibility(View.GONE);
    } else if (messageRecord.isFailed()) {
      errorText.setVisibility(View.VISIBLE);
      resendButton.setVisibility(View.GONE);
      resendButton.setOnClickListener(null);
      messageMetadata.setVisibility(View.GONE);
    } else {
      errorText.setVisibility(View.GONE);
      resendButton.setVisibility(View.GONE);
      resendButton.setOnClickListener(null);
      messageMetadata.setVisibility(View.VISIBLE);
    }
  }

  private void bindSentReceivedDates(MessageRecord messageRecord) {
    sentDate.setOnLongClickListener(null);
    receivedDate.setOnLongClickListener(null);

    if (messageRecord.isPending() || messageRecord.isFailed()) {
      sentDate.setText("-");
      receivedGroup.setVisibility(View.GONE);
    } else {
      Locale dateLocale    = Locale.getDefault();
      SimpleDateFormat dateFormatter = DateUtils.getDetailedDateFormatter(itemView.getContext(), dateLocale);
      sentDate.setText(dateFormatter.format(new Date(messageRecord.getDateSent())));
      sentDate.setOnLongClickListener(v -> {
        copyToClipboard(String.valueOf(messageRecord.getDateSent()));
        return true;
      });

      if (messageRecord.getDateReceived() != messageRecord.getDateSent() && !messageRecord.isOutgoing()) {
        receivedDate.setText(dateFormatter.format(new Date(messageRecord.getDateReceived())));
        receivedDate.setOnLongClickListener(v -> {
          copyToClipboard(String.valueOf(messageRecord.getDateReceived()));
          return true;
        });
        receivedGroup.setVisibility(View.VISIBLE);
      } else {
        receivedGroup.setVisibility(View.GONE);
      }
    }
  }

  private void bindExpirationTime(final MessageRecord messageRecord, boolean running) {
    if (expiresUpdater != null) {
      expiresUpdater.stop();
      expiresUpdater = null;
    }

    if (messageRecord.getExpiresIn() <= 0 || messageRecord.getExpireStarted() <= 0) {
      expiresGroup.setVisibility(View.GONE);
      return;
    }

    expiresGroup.setVisibility(View.VISIBLE);
    if (running) {
      expiresUpdater = new ExpiresUpdater(messageRecord);
      Util.runOnMain(expiresUpdater);
    }
  }

  private void bindTransport(MessageRecord messageRecord) {
    final String transportText;
    if (messageRecord.isOutgoing() && messageRecord.isFailed()) {
      transportText = "-";
    } else if (messageRecord.isPending()) {
      transportText = itemView.getContext().getString(R.string.ConversationFragment_pending);
    } else if (messageRecord.isPush()) {
      transportText = itemView.getContext().getString(R.string.ConversationFragment_push);
    } else if (messageRecord.isMms()) {
      transportText = itemView.getContext().getString(R.string.ConversationFragment_mms);
    } else {
      transportText = itemView.getContext().getString(R.string.ConversationFragment_sms);
    }

    transport.setText(transportText);
  }

  private void copyToClipboard(String text) {
    ((ClipboardManager) itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("text", text));
  }

  private class ExpiresUpdater implements Runnable {

    private final long    expireStartedTimestamp;
    private final long    expiresInTimestamp;
    private       boolean running;

    ExpiresUpdater(MessageRecord messageRecord) {
      expireStartedTimestamp = messageRecord.getExpireStarted();
      expiresInTimestamp     = messageRecord.getExpiresIn();
      running                = true;
    }

    @Override
    public void run() {
      long   elapsed        = System.currentTimeMillis() - expireStartedTimestamp;
      long   remaining      = expiresInTimestamp - elapsed;
      int    expirationTime = Math.max((int) (remaining / 1000), 1);
      String duration       = ExpirationUtil.getExpirationDisplayValue(itemView.getContext(), expirationTime);

      expiresIn.setText(duration);

      if (running && expirationTime > 1) {
        Util.runOnMainDelayed(this, 500);
      }
    }

    void stop() {
      running = false;
    }
  }
}
