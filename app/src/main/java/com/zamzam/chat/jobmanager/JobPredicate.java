package com.zamzam.chat.jobmanager;

import androidx.annotation.NonNull;

import com.zamzam.chat.jobmanager.persistence.JobSpec;

public interface JobPredicate {
  JobPredicate NONE = jobSpec -> true;

  boolean shouldRun(@NonNull JobSpec jobSpec);
}
