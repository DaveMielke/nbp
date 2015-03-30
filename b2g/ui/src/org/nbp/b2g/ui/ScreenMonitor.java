package org.nbp.b2g.ui;

import android.util.Log;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityEvent;

import android.content.Intent;

public class ScreenMonitor extends AccessibilityService {
  private final static String LOG_TAG = ScreenMonitor.class.getName();

  private static ScreenMonitor screenMonitor = null;

  public static ScreenMonitor getScreenMonitor () {
    if (screenMonitor == null) Log.w(LOG_TAG, "screen monitor not runnig");
    return screenMonitor;
  }

  @Override
  public void onCreate () {
    super.onCreate();
    Log.d(LOG_TAG, "screen monitor started");
    screenMonitor = this;
    KeyboardMonitor.startKeyboardMonitor();
  }

  @Override
  public void onDestroy () {
    super.onDestroy();
    screenMonitor = null;
    Log.d(LOG_TAG, "screen monitor stopped");
  }

  @Override
  protected void onServiceConnected () {
    Log.d(LOG_TAG, "screen monitor connected");

    AccessibilityNodeInfo node = ScreenUtilities.getCurrentNode();
    if (node != null) {
      BrailleDevice.write(node);
    } else {
      BrailleDevice.write("B2G ready");
    }
  }

  @Override
  public boolean onUnbind (Intent intent) {
    Log.d(LOG_TAG, "screen monitor disconnected");
    return false;
  }

  @Override
  public void onAccessibilityEvent (AccessibilityEvent event) {
    if (ApplicationParameters.LOG_ACCESSIBILITY_EVENTS) {
      Log.d(LOG_TAG, "accessibility event: " + event.toString());
    }

    switch (event.getEventType()) {
      case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
      case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {
        AccessibilityNodeInfo node = ScreenUtilities.getCurrentNode();

        if (node != null) {
          BrailleDevice.write(node);
          node.recycle();
        }

        break;
      }

      default:
        break;
    }
  }

  @Override
  public void onInterrupt () {
  }
}
