package com.zamzam.chat.groups.ui.creategroup.details;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;

import com.zamzam.chat.PassphraseRequiredActivity;
import com.zamzam.chat.R;
import com.zamzam.chat.conversation.ConversationActivity;
import com.zamzam.chat.database.ThreadDatabase;
import com.zamzam.chat.groups.ui.managegroup.dialogs.GroupInviteSentDialog;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.recipients.RecipientId;
import com.zamzam.chat.util.DynamicNoActionBarTheme;
import com.zamzam.chat.util.DynamicTheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AddGroupDetailsActivity extends PassphraseRequiredActivity implements AddGroupDetailsFragment.Callback {

  private static final String EXTRA_RECIPIENTS = "recipient_ids";

  private final DynamicTheme theme = new DynamicNoActionBarTheme();

  public static Intent newIntent(@NonNull Context context, @NonNull Collection<RecipientId> recipients) {
    Intent intent = new Intent(context, AddGroupDetailsActivity.class);

    intent.putParcelableArrayListExtra(EXTRA_RECIPIENTS, new ArrayList<>(recipients));

    return intent;
  }

  @Override
  protected void onCreate(@Nullable Bundle bundle, boolean ready) {
    theme.onCreate(this);

    setContentView(R.layout.add_group_details_activity);

    if (bundle == null) {
      ArrayList<RecipientId>      recipientIds = getIntent().getParcelableArrayListExtra(EXTRA_RECIPIENTS);
      AddGroupDetailsFragmentArgs arguments    = new AddGroupDetailsFragmentArgs.Builder(recipientIds.toArray(new RecipientId[0])).build();
      NavGraph                    graph        = Navigation.findNavController(this, R.id.nav_host_fragment).getGraph();

      Navigation.findNavController(this, R.id.nav_host_fragment).setGraph(graph, arguments.toBundle());
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    theme.onResume(this);
  }

  @Override
  public void onGroupCreated(@NonNull RecipientId recipientId,
                             long threadId,
                             @NonNull List<Recipient> invitedMembers)
  {
    Dialog dialog = GroupInviteSentDialog.showInvitesSent(this, invitedMembers);
    if (dialog != null) {
      dialog.setOnDismissListener((d) -> goToConversation(recipientId, threadId));
    } else {
      goToConversation(recipientId, threadId);
    }
  }

  void goToConversation(@NonNull RecipientId recipientId, long threadId) {
    Intent intent = ConversationActivity.buildIntent(this,
                                                     recipientId,
                                                     threadId,
                                                     ThreadDatabase.DistributionTypes.DEFAULT,
                                                     -1);

    startActivity(intent);
    setResult(RESULT_OK);
    finish();
  }

  @Override
  public void onNavigationButtonPressed() {
    setResult(RESULT_CANCELED);
    finish();
  }
}
