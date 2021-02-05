package com.zamzam.chat.longmessage;

import android.text.TextUtils;

import com.zamzam.chat.database.model.MessageRecord;

/**
 * A wrapper around a {@link MessageRecord} and its extra text attachment expanded into a string
 * held in memory.
 */
class LongMessage {

  private final MessageRecord messageRecord;
  private final String        fullBody;

  LongMessage(MessageRecord messageRecord, String fullBody) {
    this.messageRecord = messageRecord;
    this.fullBody      = fullBody;
  }

  MessageRecord getMessageRecord() {
    return messageRecord;
  }

  String getFullBody() {
    return !TextUtils.isEmpty(fullBody) ? fullBody : messageRecord.getBody();
  }
}
