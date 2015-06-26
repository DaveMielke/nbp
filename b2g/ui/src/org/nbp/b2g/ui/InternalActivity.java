package org.nbp.b2g.ui;

import android.app.Activity;

import android.view.View;
import android.view.ViewGroup;

import android.widget.ScrollView;
import android.widget.HorizontalScrollView;

public abstract class InternalActivity extends Activity {
  protected ViewGroup createVerticalScrollView () {
    ScrollView view = new ScrollView(this);
    return view;
  }

  protected ViewGroup createVerticalScrollView (View content) {
    ViewGroup view = createVerticalScrollView();
    view.addView(content);
    return view;
  }

  protected ViewGroup createHorizontalScrollView () {
    HorizontalScrollView view = new HorizontalScrollView(this);
    return view;
  }

  protected ViewGroup createHorizontalScrollView (View content) {
    ViewGroup view = createHorizontalScrollView();
    view.addView(content);
    return view;
  }
}
