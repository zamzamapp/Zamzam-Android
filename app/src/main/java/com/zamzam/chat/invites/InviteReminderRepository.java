package com.zamzam.chat.invites;

import android.content.Context;

import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.database.MmsSmsDatabase;
import com.zamzam.chat.database.RecipientDatabase;
import com.zamzam.chat.recipients.Recipient;

public final class InviteReminderRepository implements InviteReminderModel.Repository {

  private final Context context;

  public InviteReminderRepository(Context context) {
    this.context = context;
  }

  @Override
  public void setHasSeenFirstInviteReminder(Recipient recipient) {
    RecipientDatabase recipientDatabase = DatabaseFactory.getRecipientDatabase(context);
    recipientDatabase.setSeenFirstInviteReminder(recipient.getId());
  }

  @Override
  public void setHasSeenSecondInviteReminder(Recipient recipient) {
    RecipientDatabase recipientDatabase = DatabaseFactory.getRecipientDatabase(context);
    recipientDatabase.setSeenSecondInviteReminder(recipient.getId());
  }

  @Override
  public int getPercentOfInsecureMessages(int insecureCount) {
    MmsSmsDatabase mmsSmsDatabase = DatabaseFactory.getMmsSmsDatabase(context);
    int            insecure       = mmsSmsDatabase.getInsecureMessageCountForInsights();
    int            secure         = mmsSmsDatabase.getSecureMessageCountForInsights();

    if (insecure + secure == 0) return 0;
    return Math.round(100f * (insecureCount / (float) (insecure + secure)));
  }
}
