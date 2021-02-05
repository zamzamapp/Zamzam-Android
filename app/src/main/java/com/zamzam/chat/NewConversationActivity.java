/*
 * Copyright (C) 2015 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.zamzam.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;

import com.zamzam.chat.contacts.sync.DirectoryHelper;
import com.zamzam.chat.conversation.ConversationActivity;
import com.zamzam.chat.database.DatabaseFactory;
import com.zamzam.chat.database.ThreadDatabase;
import com.zamzam.chat.groups.ui.creategroup.CreateGroupActivity;
import com.zamzam.chat.jobmanager.impl.NetworkConstraint;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.recipients.RecipientId;
import com.zamzam.chat.util.FeatureFlags;
import com.zamzam.chat.util.concurrent.SimpleTask;
import com.zamzam.chat.util.views.SimpleProgressDialog;
import org.whispersystems.libsignal.util.guava.Optional;

import java.io.IOException;

/**
 * Activity container for starting a new conversation.
 *
 * @author Moxie Marlinspike
 *
 */
public class NewConversationActivity extends ContactSelectionActivity
                                    implements ContactSelectionListFragment.ListCallback
{

  @SuppressWarnings("unused")
  private static final String TAG = NewConversationActivity.class.getSimpleName();

  @Override
  public void onCreate(Bundle bundle, boolean ready) {
    super.onCreate(bundle, ready);
    assert getSupportActionBar() != null;
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onContactSelected(Optional<RecipientId> recipientId, String number) {
    if (recipientId.isPresent()) {
      launch(Recipient.resolved(recipientId.get()));
    } else {
      Log.i(TAG, "[onContactSelected] Maybe creating a new recipient.");
      if (FeatureFlags.cds() && NetworkConstraint.isMet(this)) {
        Log.i(TAG, "[onContactSelected] CDS enabled. Doing contact refresh.");

        AlertDialog progress = SimpleProgressDialog.show(this);

        SimpleTask.run(getLifecycle(), () -> {
          Recipient resolved = Recipient.external(this, number);

          if (!resolved.isRegistered()) {
            Log.i(TAG, "[onContactSelected] Not registered. Doing a directory refresh.");
            try {
              DirectoryHelper.refreshDirectoryFor(this, resolved, false);
              resolved = Recipient.resolved(resolved.getId());
            } catch (IOException e) {
              Log.w(TAG, "[onContactSelected] Failed to refresh directory for new contact.");
            }
          }

          return resolved;
        }, resolved -> {
          progress.dismiss();
          launch(resolved);
        });
      } else {
        launch(Recipient.external(this, number));
      }
    }

    return true;
  }

  private void launch(Recipient recipient) {
    Intent intent = new Intent(this, ConversationActivity.class);
    intent.putExtra(ConversationActivity.RECIPIENT_EXTRA, recipient.getId());
    intent.putExtra(ConversationActivity.TEXT_EXTRA, getIntent().getStringExtra(ConversationActivity.TEXT_EXTRA));
    intent.setDataAndType(getIntent().getData(), getIntent().getType());

    long existingThread = DatabaseFactory.getThreadDatabase(this).getThreadIdIfExistsFor(recipient);

    intent.putExtra(ConversationActivity.THREAD_ID_EXTRA, existingThread);
    intent.putExtra(ConversationActivity.DISTRIBUTION_TYPE_EXTRA, ThreadDatabase.DistributionTypes.DEFAULT);
    startActivity(intent);
    finish();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);

    switch (item.getItemId()) {
    case android.R.id.home:   super.onBackPressed(); return true;
    case R.id.menu_refresh:   handleManualRefresh(); return true;
    case R.id.menu_new_group: handleCreateGroup();   return true;
    case R.id.menu_invite:    handleInvite();        return true;
    }

    return false;
  }

  private void handleManualRefresh() {
    contactsFragment.setRefreshing(true);
    onRefresh();
  }

  private void handleCreateGroup() {
    startActivity(CreateGroupActivity.newIntent(this));
  }

  private void handleInvite() {
    startActivity(new Intent(this, InviteActivity.class));
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    getMenuInflater().inflate(R.menu.new_conversation_activity, menu);

    super.onPrepareOptionsMenu(menu);
    return true;
  }

  @Override
  public void onInvite() {
    handleInvite();
    finish();
  }

  @Override
  public void onNewGroup(boolean forceV1) {
    handleCreateGroup();
    finish();
  }
}
