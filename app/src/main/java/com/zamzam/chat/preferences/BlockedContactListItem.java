package com.zamzam.chat.preferences;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zamzam.chat.R;
import com.zamzam.chat.components.AvatarImageView;
import com.zamzam.chat.mms.GlideRequests;
import com.zamzam.chat.recipients.LiveRecipient;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.recipients.RecipientForeverObserver;

public class BlockedContactListItem extends RelativeLayout implements RecipientForeverObserver {

  private AvatarImageView contactPhotoImage;
  private TextView        nameView;
  private GlideRequests   glideRequests;
  private LiveRecipient   recipient;

  public BlockedContactListItem(Context context) {
    super(context);
  }

  public BlockedContactListItem(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public BlockedContactListItem(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();
    this.contactPhotoImage = findViewById(R.id.contact_photo_image);
    this.nameView          = findViewById(R.id.name);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (this.recipient != null) {
      recipient.removeForeverObserver(this);
    }
  }

  public void set(@NonNull GlideRequests glideRequests, @NonNull LiveRecipient recipient) {
    this.glideRequests = glideRequests;
    this.recipient     = recipient;

    onRecipientChanged(recipient.get());

    this.recipient.observeForever(this);
  }

  @Override
  public void onRecipientChanged(@NonNull Recipient recipient) {
    final AvatarImageView contactPhotoImage = this.contactPhotoImage;
    final TextView        nameView          = this.nameView;

    contactPhotoImage.setAvatar(glideRequests, recipient, false);
    nameView.setText(recipient.getDisplayName(getContext()));
  }

  public Recipient getRecipient() {
    return recipient.get();
  }
}
