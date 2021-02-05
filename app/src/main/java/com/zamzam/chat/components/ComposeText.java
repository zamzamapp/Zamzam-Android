package com.zamzam.chat.components;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.method.QwertyKeyListener;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.BuildCompat;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;

import com.zamzam.chat.R;
import com.zamzam.chat.TransportOption;
import com.zamzam.chat.components.emoji.EmojiEditText;
import com.zamzam.chat.components.mention.MentionAnnotation;
import com.zamzam.chat.components.mention.MentionRendererDelegate;
import com.zamzam.chat.logging.Log;
import com.zamzam.chat.util.FeatureFlags;
import com.zamzam.chat.util.TextSecurePreferences;

import java.util.UUID;

public class ComposeText extends EmojiEditText {

  private CharSequence            hint;
  private SpannableString         subHint;
  private MentionRendererDelegate mentionRendererDelegate;

  @Nullable private InputPanel.MediaListener      mediaListener;
  @Nullable private CursorPositionChangedListener cursorPositionChangedListener;
  @Nullable private MentionQueryChangedListener   mentionQueryChangedListener;

  public ComposeText(Context context) {
    super(context);
    initialize();
  }

  public ComposeText(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public ComposeText(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  public String getTextTrimmed(){
    return getText().toString().trim();
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    if (!TextUtils.isEmpty(hint)) {
      if (!TextUtils.isEmpty(subHint)) {
        setHint(new SpannableStringBuilder().append(ellipsizeToWidth(hint))
                                            .append("\n")
                                            .append(ellipsizeToWidth(subHint)));
      } else {
        setHint(ellipsizeToWidth(hint));
      }
    }
  }

  @Override
  protected void onSelectionChanged(int selStart, int selEnd) {
    super.onSelectionChanged(selStart, selEnd);

    if (FeatureFlags.mentions()) {
      if (selStart == selEnd) {
        doAfterCursorChange();
      } else {
        updateQuery("");
      }
    }

    if (cursorPositionChangedListener != null) {
      cursorPositionChangedListener.onCursorPositionChanged(selStart, selEnd);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (FeatureFlags.mentions() && getText() != null && getLayout() != null) {
      int checkpoint = canvas.save();
      canvas.translate(getTotalPaddingLeft(), getTotalPaddingTop());
      try {
        mentionRendererDelegate.draw(canvas, getText(), getLayout());
      } finally {
        canvas.restoreToCount(checkpoint);
      }
    }
    super.onDraw(canvas);
  }

  private CharSequence ellipsizeToWidth(CharSequence text) {
    return TextUtils.ellipsize(text,
                               getPaint(),
                               getWidth() - getPaddingLeft() - getPaddingRight(),
                               TruncateAt.END);
  }

  public void setHint(@NonNull String hint, @Nullable CharSequence subHint) {
    this.hint = hint;

    if (subHint != null) {
      this.subHint = new SpannableString(subHint);
      this.subHint.setSpan(new RelativeSizeSpan(0.5f), 0, subHint.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    } else {
      this.subHint = null;
    }

    if (this.subHint != null) {
      super.setHint(new SpannableStringBuilder().append(ellipsizeToWidth(this.hint))
                                                .append("\n")
                                                .append(ellipsizeToWidth(this.subHint)));
    } else {
      super.setHint(ellipsizeToWidth(this.hint));
    }
  }

  public void appendInvite(String invite) {
    if (!TextUtils.isEmpty(getText()) && !getText().toString().equals(" ")) {
      append(" ");
    }

    append(invite);
    setSelection(getText().length());
  }

  public void setCursorPositionChangedListener(@Nullable CursorPositionChangedListener listener) {
    this.cursorPositionChangedListener = listener;
  }

  public void setMentionQueryChangedListener(@Nullable MentionQueryChangedListener listener) {
    this.mentionQueryChangedListener = listener;
  }

  private boolean isLandscape() {
    return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
  }

  public void setTransport(TransportOption transport) {
    final boolean useSystemEmoji = TextSecurePreferences.isSystemEmojiPreferred(getContext());
    final boolean isIncognito    = TextSecurePreferences.isIncognitoKeyboardEnabled(getContext());

    int imeOptions = (getImeOptions() & ~EditorInfo.IME_MASK_ACTION) | EditorInfo.IME_ACTION_SEND;
    int inputType  = getInputType();

    if (isLandscape()) setImeActionLabel(transport.getComposeHint(), EditorInfo.IME_ACTION_SEND);
    else               setImeActionLabel(null, 0);

    if (useSystemEmoji) {
      inputType = (inputType & ~InputType.TYPE_MASK_VARIATION) | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE;
    }

    setInputType(inputType);
    setImeOptions(imeOptions);
    setHint(transport.getComposeHint(),
            transport.getSimName().isPresent()
                ? getContext().getString(R.string.conversation_activity__from_sim_name, transport.getSimName().get())
                : null);
  }

  @Override
  public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
    InputConnection inputConnection = super.onCreateInputConnection(editorInfo);

    if(TextSecurePreferences.isEnterSendsEnabled(getContext())) {
      editorInfo.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
    }

    if (Build.VERSION.SDK_INT < 21) return inputConnection;
    if (mediaListener == null)      return inputConnection;
    if (inputConnection == null)    return null;

    EditorInfoCompat.setContentMimeTypes(editorInfo, new String[] {"image/jpeg", "image/png", "image/gif"});
    return InputConnectionCompat.createWrapper(inputConnection, editorInfo, new CommitContentListener(mediaListener));
  }

  public void setMediaListener(@Nullable InputPanel.MediaListener mediaListener) {
    this.mediaListener = mediaListener;
  }

  private void initialize() {
    if (TextSecurePreferences.isIncognitoKeyboardEnabled(getContext())) {
      setImeOptions(getImeOptions() | 16777216);
    }

    if (FeatureFlags.mentions()) {
      mentionRendererDelegate = new MentionRendererDelegate(getContext());
    }
  }

  private void doAfterCursorChange() {
    Editable text = getText();
    if (text != null && enoughToFilter(text)) {
      performFiltering(text);
    } else {
      updateQuery("");
    }
  }

  private void performFiltering(@NonNull Editable text) {
    int          end   = getSelectionEnd();
    int          start = findQueryStart(text, end);
    CharSequence query = text.subSequence(start, end);
    updateQuery(query);
  }

  private void updateQuery(@NonNull CharSequence query) {
    if (mentionQueryChangedListener != null) {
      mentionQueryChangedListener.onQueryChanged(query);
    }
  }

  private boolean enoughToFilter(@NonNull Editable text) {
    int end = getSelectionEnd();
    if (end < 0) {
      return false;
    }
    return end - findQueryStart(text, end) >= 1;
  }

  public void replaceTextWithMention(@NonNull String displayName, @NonNull UUID uuid) {
    Editable text = getText();
    if (text == null) {
      return;
    }

    clearComposingText();

    int    end      = getSelectionEnd();
    int    start    = findQueryStart(text, end) - 1;
    String original = TextUtils.substring(text, start, end);

    QwertyKeyListener.markAsReplaced(text, start, end, original);
    text.replace(start, end, createReplacementToken(displayName, uuid));
  }

  private @NonNull CharSequence createReplacementToken(@NonNull CharSequence text, @NonNull UUID uuid) {
    SpannableStringBuilder builder = new SpannableStringBuilder("@");
    if (text instanceof Spanned) {
      SpannableString spannableString = new SpannableString(text + " ");
      TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, spannableString, 0);
      builder.append(spannableString);
    } else {
      builder.append(text).append(" ");
    }

    builder.setSpan(MentionAnnotation.mentionAnnotationForUuid(uuid), 0, builder.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

    return builder;
  }

  private int findQueryStart(@NonNull CharSequence text, int inputCursorPosition) {
    if (inputCursorPosition == 0) {
      return inputCursorPosition;
    }

    int delimiterSearchIndex = inputCursorPosition - 1;
    while (delimiterSearchIndex >= 0 && (text.charAt(delimiterSearchIndex) != '@' && text.charAt(delimiterSearchIndex) != ' ')) {
      delimiterSearchIndex--;
    }

    if (delimiterSearchIndex >= 0 && text.charAt(delimiterSearchIndex) == '@') {
      return delimiterSearchIndex + 1;
    }
    return inputCursorPosition;
  }

  private static class CommitContentListener implements InputConnectionCompat.OnCommitContentListener {

    private static final String TAG = CommitContentListener.class.getSimpleName();

    private final InputPanel.MediaListener mediaListener;

    private CommitContentListener(@NonNull InputPanel.MediaListener mediaListener) {
      this.mediaListener = mediaListener;
    }

    @Override
    public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags, Bundle opts) {
      if (BuildCompat.isAtLeastNMR1() && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
        try {
          inputContentInfo.requestPermission();
        } catch (Exception e) {
          Log.w(TAG, e);
          return false;
        }
      }

      if (inputContentInfo.getDescription().getMimeTypeCount() > 0) {
        mediaListener.onMediaSelected(inputContentInfo.getContentUri(),
                                      inputContentInfo.getDescription().getMimeType(0));

        return true;
      }

      return false;
    }
  }

  public interface CursorPositionChangedListener {
    void onCursorPositionChanged(int start, int end);
  }

  public interface MentionQueryChangedListener {
    void onQueryChanged(CharSequence query);
  }
}
