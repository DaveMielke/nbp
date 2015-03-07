package org.nbp.b2g.input;

import android.util.Log;

import android.view.inputmethod.InputConnection;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

public class KeyAction extends Action {
  private static final String LOG_TAG = KeyAction.class.getName();

  protected final int keyCode;

  protected void logKeyEvent (String action) {
    if (ApplicationParameters.LOG_PERFORMED_ACTIONS) {
      Log.d(LOG_TAG, "sending key " + action + ": " + getName());
    }
  }

  protected boolean sendKey (int interval) {
    InputService inputService = getInputService();

    if (inputService != null) {
      InputConnection connection = inputService.getCurrentInputConnection();

      if (connection != null) {
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        logKeyEvent("press");

        if (connection.sendKeyEvent(event)) {
          if (interval > 0) delay(interval);
          event = KeyEvent.changeAction(event, KeyEvent.ACTION_UP);
          logKeyEvent("release");

          if (connection.sendKeyEvent(event)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  protected boolean sendKey (boolean longPress) {
    int interval = longPress? (ViewConfiguration.getLongPressTimeout() + ApplicationParameters.LONG_PRESS_DELAY): 0;
    return sendKey(interval);
  }

  protected boolean sendKey () {
    return sendKey(false);
  }

  @Override
  public boolean performAction () {
    return sendKey();
  }

  protected KeyAction (int keyCode, String name) {
    super(name);
    this.keyCode = keyCode;
  }

  public KeyAction (int keyCode) {
    this(keyCode, KeyEvent.keyCodeToString(keyCode));
  }

  public static void add (int keyMask, int keyCode) {
    add(keyMask, new KeyAction(keyCode));
  }
}
