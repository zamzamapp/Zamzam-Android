package com.zamzam.chat.jobmanager.impl;

import androidx.annotation.NonNull;

import com.zamzam.chat.dependencies.ApplicationDependencies;
import com.zamzam.chat.jobmanager.ConstraintObserver;

/**
 * An observer for {@link WebsocketDrainedConstraint}. Will fire when the websocket is drained
 * (i.e. it has received an empty response).
 */
public class WebsocketDrainedConstraintObserver implements ConstraintObserver {

  private static final String REASON = WebsocketDrainedConstraintObserver.class.getSimpleName();

  private volatile Notifier notifier;

  public WebsocketDrainedConstraintObserver() {
    ApplicationDependencies.getIncomingMessageObserver().addWebsocketDrainedListener(() -> {
      if (notifier != null) {
        notifier.onConstraintMet(REASON);
      }
    });
  }

  @Override
  public void register(@NonNull Notifier notifier) {
    this.notifier = notifier;
  }
}
