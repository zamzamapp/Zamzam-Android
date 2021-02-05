package com.zamzam.chat.crypto;

import android.content.Context;
import androidx.annotation.NonNull;

import com.zamzam.chat.crypto.storage.TextSecureSessionStore;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.recipients.RecipientId;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.SessionStore;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;

public class SessionUtil {

  public static boolean hasSession(@NonNull Context context, @NonNull RecipientId id) {
    SessionStore          sessionStore   = new TextSecureSessionStore(context);
    SignalProtocolAddress axolotlAddress = new SignalProtocolAddress(Recipient.resolved(id).requireServiceId(), SignalServiceAddress.DEFAULT_DEVICE_ID);

    return sessionStore.containsSession(axolotlAddress);
  }

  public static void archiveSiblingSessions(Context context, SignalProtocolAddress address) {
    TextSecureSessionStore  sessionStore = new TextSecureSessionStore(context);
    sessionStore.archiveSiblingSessions(address);
  }

  public static void archiveAllSessions(Context context) {
    new TextSecureSessionStore(context).archiveAllSessions();
  }

}
