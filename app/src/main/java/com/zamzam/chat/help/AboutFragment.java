package com.zamzam.chat.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zamzam.chat.ApplicationPreferencesActivity;
import com.zamzam.chat.LoggingFragment;
import com.zamzam.chat.R;

public class AboutFragment extends LoggingFragment {

//  private EditText               problem;
//  private CheckBox               includeDebugLogs;
//  private View                   debugLogInfo;
//  private View                   faq;
//  private CircularProgressButton next;
//  private View                   toaster;
//  private List<EmojiImageView>   emoji;
//  private HelpViewModel          helpViewModel;

  @Override
  public @Nullable View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.activity_about, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    initializeViews(view);
  }

  @Override
  public void onResume() {
    super.onResume();
    ((ApplicationPreferencesActivity) getActivity()).getSupportActionBar().setTitle(R.string.preferences__about);
  }

  private void initializeViews(@NonNull View view) {
//    problem          = view.findViewById(R.id.help_fragment_problem);
//    includeDebugLogs = view.findViewById(R.id.help_fragment_debug);
//    debugLogInfo     = view.findViewById(R.id.help_fragment_debug_info);
//    faq              = view.findViewById(R.id.help_fragment_faq);
//    next             = view.findViewById(R.id.help_fragment_next);
//    toaster          = view.findViewById(R.id.help_fragment_next_toaster);
  }
}
