package org.nbp.b2g.input;

import android.util.Log;

public class KeyEvents {
  private final static String LOG_TAG = KeyEvents.class.getName();

  private static int pressedKeyMask = 0;
  private static int activeKeyMask = 0;

  private static boolean performAction (Action action) {
    if (ApplicationParameters.LOG_PERFORMED_ACTIONS) {
      Log.d(LOG_TAG, "performing action: " + action.getName());
    }

    try {
      if (action.performAction()) return true;
      Log.w(LOG_TAG, "action failed: " + action.getName());
      ApplicationUtilities.beep();
    } catch (Exception exception) {
      Log.w(LOG_TAG, "action crashed: " + action.getName(), exception);
    }

    return false;
  }

  public static int getKeyMask () {
    return activeKeyMask;
  }

  public static void handleKeyPress (int keyMask) {
    if (keyMask != 0) {
      if ((pressedKeyMask & keyMask) == 0) {
        pressedKeyMask |= keyMask;
        activeKeyMask = pressedKeyMask;
      }
    }
  }

  public static void handleKeyRelease (int keyMask) {
    if (keyMask != 0) {
      if (activeKeyMask > 0) {
        boolean performed = false;
        Action action = KeyBindings.getAction(activeKeyMask);

        if (action != null) {
          if (performAction(action)) {
            performed = true;
          }
        }

        if (!performed) ApplicationUtilities.beep();
        activeKeyMask = 0;
      }

      pressedKeyMask &= ~keyMask;
    }
  }

  public static void handleKeyEvent (int keyMask, boolean press) {
    if (press) {
      handleKeyPress(keyMask);
    } else {
      handleKeyRelease(keyMask);
    }
  }

  public static void resetKeys () {
    if (ApplicationParameters.LOG_KEY_EVENTS) {
      Log.d(LOG_TAG, "resetting key state");
    }

    pressedKeyMask = 0;
    activeKeyMask = pressedKeyMask;
  }

  private KeyEvents () {
  }
}
