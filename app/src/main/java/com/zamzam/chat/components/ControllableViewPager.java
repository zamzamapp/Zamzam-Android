package com.zamzam.chat.components;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.zamzam.chat.components.viewpager.HackyViewPager;

/**
 * An implementation of {@link ViewPager} that disables swiping when the view is disabled.
 */
public class ControllableViewPager extends HackyViewPager {

  public ControllableViewPager(@NonNull Context context) {
    super(context);
  }

  public ControllableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    return isEnabled() && super.onTouchEvent(ev);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    return isEnabled() && super.onInterceptTouchEvent(ev);
  }
}
