package com.zamzam.chat.mms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.mms.pdu_alt.SendConf;

import com.zamzam.chat.transport.UndeliverableMessageException;


public interface OutgoingMmsConnection {
  @Nullable
  SendConf send(@NonNull byte[] pduBytes, int subscriptionId) throws UndeliverableMessageException;
}
