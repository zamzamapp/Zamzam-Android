package com.zamzam.chat.groups.ui.creategroup.details;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dd.CircularProgressButton;

import com.zamzam.chat.LoggingFragment;
import com.zamzam.chat.R;
import com.zamzam.chat.groups.ui.GroupMemberListView;
import com.zamzam.chat.mediasend.AvatarSelectionActivity;
import com.zamzam.chat.mediasend.AvatarSelectionBottomSheetDialogFragment;
import com.zamzam.chat.mediasend.Media;
import com.zamzam.chat.mms.DecryptableStreamUriLoader;
import com.zamzam.chat.mms.GlideApp;
import com.zamzam.chat.permissions.Permissions;
import com.zamzam.chat.profiles.AvatarHelper;
import com.zamzam.chat.recipients.Recipient;
import com.zamzam.chat.recipients.RecipientId;
import com.zamzam.chat.util.BitmapUtil;
import com.zamzam.chat.util.ViewUtil;
import com.zamzam.chat.util.text.AfterTextChanged;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AddGroupDetailsFragment extends LoggingFragment {

  private static final int   AVATAR_PLACEHOLDER_INSET_DP = 18;
  private static final short REQUEST_CODE_AVATAR         = 27621;

  private CircularProgressButton   create;
  private Callback                 callback;
  private AddGroupDetailsViewModel viewModel;
  private Drawable                 avatarPlaceholder;
  private EditText                 name;
  private Toolbar                  toolbar;

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    if (context instanceof Callback) {
      callback = (Callback) context;
    } else {
      throw new ClassCastException("Parent context should implement AddGroupDetailsFragment.Callback");
    }
  }

  @Override
  public @Nullable View onCreateView(@NonNull LayoutInflater inflater,
                                     @Nullable ViewGroup container,
                                     @Nullable Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.add_group_details_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    create  = view.findViewById(R.id.create);
    name    = view.findViewById(R.id.name);
    toolbar = view.findViewById(R.id.toolbar);

    setCreateEnabled(false, false);

    GroupMemberListView members    = view.findViewById(R.id.member_list);
    ImageView           avatar     = view.findViewById(R.id.group_avatar);
    View                mmsWarning = view.findViewById(R.id.mms_warning);

    avatarPlaceholder = VectorDrawableCompat.create(getResources(), R.drawable.ic_camera_outline_32_ultramarine, requireActivity().getTheme());

    if (savedInstanceState == null) {
      avatar.setImageDrawable(new InsetDrawable(avatarPlaceholder, ViewUtil.dpToPx(AVATAR_PLACEHOLDER_INSET_DP)));
    }

    initializeViewModel();

    avatar.setOnClickListener(v -> showAvatarSelectionBottomSheet());
    members.setRecipientClickListener(this::handleRecipientClick);
    name.addTextChangedListener(new AfterTextChanged(editable -> viewModel.setName(editable.toString())));
    toolbar.setNavigationOnClickListener(unused -> callback.onNavigationButtonPressed());
    create.setOnClickListener(v -> handleCreateClicked());
    viewModel.getMembers().observe(getViewLifecycleOwner(), recipients -> {
      members.setMembers(recipients);
      if (recipients.isEmpty()) {
        toast(R.string.AddGroupDetailsFragment__groups_require_at_least_two_members);
        callback.onNavigationButtonPressed();
      }
    });
    viewModel.getCanSubmitForm().observe(getViewLifecycleOwner(), isFormValid -> setCreateEnabled(isFormValid, true));
    viewModel.getIsMms().observe(getViewLifecycleOwner(), isMms -> {
      mmsWarning.setVisibility(isMms ? View.VISIBLE : View.GONE);
      name.setVisibility(isMms ? View.GONE : View.VISIBLE);
      avatar.setVisibility(isMms ? View.GONE : View.VISIBLE);
      toolbar.setTitle(isMms ? R.string.AddGroupDetailsFragment__create_group : R.string.AddGroupDetailsFragment__name_this_group);
    });
    viewModel.getAvatar().observe(getViewLifecycleOwner(), avatarBytes -> {
      if (avatarBytes == null) {
        avatar.setImageDrawable(new InsetDrawable(avatarPlaceholder, ViewUtil.dpToPx(AVATAR_PLACEHOLDER_INSET_DP)));
      } else {
        GlideApp.with(this)
                .load(avatarBytes)
                .circleCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(avatar);
      }
    });

    name.requestFocus();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == REQUEST_CODE_AVATAR && resultCode == Activity.RESULT_OK && data != null) {

      if (data.getBooleanExtra("delete", false)) {
        viewModel.setAvatar(null);
        return;
      }

      final Media                                     result         = data.getParcelableExtra(AvatarSelectionActivity.EXTRA_MEDIA);
      final DecryptableStreamUriLoader.DecryptableUri decryptableUri = new DecryptableStreamUriLoader.DecryptableUri(result.getUri());

      GlideApp.with(this)
              .asBitmap()
              .load(decryptableUri)
              .skipMemoryCache(true)
              .diskCacheStrategy(DiskCacheStrategy.NONE)
              .centerCrop()
              .override(AvatarHelper.AVATAR_DIMENSIONS, AvatarHelper.AVATAR_DIMENSIONS)
              .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                  viewModel.setAvatar(Objects.requireNonNull(BitmapUtil.toByteArray(resource)));
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                }
              });
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private void initializeViewModel() {
    AddGroupDetailsFragmentArgs      args       = AddGroupDetailsFragmentArgs.fromBundle(requireArguments());
    AddGroupDetailsRepository        repository = new AddGroupDetailsRepository(requireContext());
    AddGroupDetailsViewModel.Factory factory    = new AddGroupDetailsViewModel.Factory(Arrays.asList(args.getRecipientIds()), repository);

    viewModel = ViewModelProviders.of(this, factory).get(AddGroupDetailsViewModel.class);

    viewModel.getGroupCreateResult().observe(getViewLifecycleOwner(), this::handleGroupCreateResult);
  }

  private void handleCreateClicked() {
    create.setClickable(false);
    create.setIndeterminateProgressMode(true);
    create.setProgress(50);

    viewModel.create();
  }

  private void handleRecipientClick(@NonNull Recipient recipient) {
    new AlertDialog.Builder(requireContext())
                   .setMessage(getString(R.string.AddGroupDetailsFragment__remove_s_from_this_group, recipient.getDisplayName(requireContext())))
                   .setCancelable(true)
                   .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                   .setPositiveButton(R.string.AddGroupDetailsFragment__remove, (dialog, which) -> {
                     viewModel.delete(recipient.getId());
                     dialog.dismiss();
                   })
                   .show();
  }

  private void handleGroupCreateResult(@NonNull GroupCreateResult groupCreateResult) {
    groupCreateResult.consume(this::handleGroupCreateResultSuccess, this::handleGroupCreateResultError);
  }

  private void handleGroupCreateResultSuccess(@NonNull GroupCreateResult.Success success) {
    callback.onGroupCreated(success.getGroupRecipient().getId(), success.getThreadId(), success.getInvitedMembers());
  }

  private void handleGroupCreateResultError(@NonNull GroupCreateResult.Error error) {
    switch (error.getErrorType()) {
      case ERROR_IO:
      case ERROR_BUSY:
        toast(R.string.AddGroupDetailsFragment__try_again_later);
        break;
      case ERROR_FAILED:
        toast(R.string.AddGroupDetailsFragment__group_creation_failed);
        break;
      case ERROR_INVALID_NAME:
        name.setError(getString(R.string.AddGroupDetailsFragment__this_field_is_required));
        break;
      case ERROR_INVALID_MEMBER_COUNT:
        toast(R.string.AddGroupDetailsFragment__groups_require_at_least_two_members);
        callback.onNavigationButtonPressed();
        break;
      default:
        throw new IllegalStateException("Unexpected error: " + error.getErrorType().name());
    }
  }

  private void toast(@StringRes int toastStringId) {
    Toast.makeText(requireContext(), toastStringId, Toast.LENGTH_SHORT)
         .show();
  }

  private void setCreateEnabled(boolean isEnabled, boolean animate) {
    if (create.isEnabled() == isEnabled) {
      return;
    }

    create.setEnabled(isEnabled);
    create.animate()
          .setDuration(animate ? 300 : 0)
          .alpha(isEnabled ? 1f : 0.5f);
  }

  private void showAvatarSelectionBottomSheet() {
    Permissions.with(this)
               .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
               .ifNecessary()
               .onAnyResult(() -> AvatarSelectionBottomSheetDialogFragment.create(viewModel.hasAvatar(), true, REQUEST_CODE_AVATAR, true)
                                                                          .show(getChildFragmentManager(), "BOTTOM"))
               .execute();
  }

  public interface Callback {
    void onGroupCreated(@NonNull RecipientId recipientId, long threadId, @NonNull List<Recipient> invitedMembers);
    void onNavigationButtonPressed();
  }
}
