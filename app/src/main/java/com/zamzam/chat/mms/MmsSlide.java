package com.zamzam.chat.mms;


import android.content.Context;
import androidx.annotation.NonNull;

import com.zamzam.chat.attachments.Attachment;

public class MmsSlide extends ImageSlide {

  public MmsSlide(@NonNull Context context, @NonNull Attachment attachment) {
    super(context, attachment);
  }

  @NonNull
  @Override
  public String getContentDescription() {
    return "MMS";
  }

}
