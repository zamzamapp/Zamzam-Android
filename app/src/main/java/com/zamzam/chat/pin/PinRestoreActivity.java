package com.zamzam.chat.pin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zamzam.chat.MainActivity;
import com.zamzam.chat.PassphraseRequiredActivity;
import com.zamzam.chat.R;
import com.zamzam.chat.lock.v2.CreateKbsPinActivity;

public final class PinRestoreActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pin_restore_activity);
  }

  void navigateToPinCreation() {
    final Intent main      = new Intent(this, MainActivity.class);
    final Intent createPin = CreateKbsPinActivity.getIntentForPinCreate(this);
    final Intent chained   = PassphraseRequiredActivity.chainIntent(createPin, main);

    startActivity(chained);
  }
}
