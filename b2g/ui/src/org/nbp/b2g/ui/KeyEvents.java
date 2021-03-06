package org.nbp.b2g.ui;

import org.nbp.b2g.ui.actions.TypeCharacter;
import org.nbp.b2g.ui.actions.PanLeft;
import org.nbp.b2g.ui.actions.PanRight;
import org.nbp.b2g.ui.host.actions.MoveBackward;
import org.nbp.b2g.ui.host.actions.MoveForward;

import java.util.Set;
import java.util.LinkedHashSet;

import java.util.SortedSet;
import java.util.TreeSet;

import java.util.Map;
import java.util.HashMap;

import java.util.concurrent.Callable;

import org.nbp.common.Timeout;
import org.nbp.common.Tones;

import android.util.Log;
import android.os.SystemClock;
import android.os.PowerManager;

public abstract class KeyEvents {
  private final static String LOG_TAG = KeyEvents.class.getName();

  private KeyEvents () {
  }

  private static long navigationKeyReleaseTime;
  private final static KeySet activeNavigationKeys = new KeySet();
  private final static KeySet pressedNavigationKeys = new KeySet();
  private final static SortedSet<Integer> pressedCursorKeys = new TreeSet<Integer>();
  private final static Set<Integer> pressedKeyboardKeys = new LinkedHashSet<Integer>();

  private final static int oneHandCompletionKey = KeySet.SPACE;
  private static boolean oneHandNavigationKeyPressed;
  private static long oneHandSpaceTimeout;

  private final static KeySet oneHandCompletionKeySet = new KeySet(
    oneHandCompletionKey
  ).freeze();

  private final static KeySet oneHandImmediateKeys = new KeySet() {
    {
      add(CURSOR);
      add(panKeys);
      add(padKeys);
      add(volumeKeys);
    }
  }.freeze();

  private static void handleKeyboardPress (int key) {
    pressedKeyboardKeys.add(key);
  }

  private static void handleKeyboardRelease (int key) {
    if (!pressedKeyboardKeys.isEmpty()) {
      pressedKeyboardKeys.remove(key);
    } else {
      InputService.injectKeyEvent(key, false);
    }
  }

  private static boolean handleKeyboardFlush () {
    if (pressedKeyboardKeys.isEmpty()) return false;
    if (!pressedCursorKeys.isEmpty()) return false;

    int[] keys = new int[pressedKeyboardKeys.size()];
    int count = 0;

    for (Integer key : pressedKeyboardKeys) {
      if (!KeySet.isKeyboardCode(key)) return false;
      keys[count++] = key;
    }

    for (int index=0; index<count; index+=1) {
      InputService.injectKeyEvent(keys[index], true);
    }

    pressedKeyboardKeys.clear();
    return true;
  }

  public static boolean performAction (final Action action) {
    if (action.editsInput()) {
      if (!ApplicationSettings.INPUT_EDITING) {
        Controls.inputEditing.confirmValue();
        return false;
      }
    }

    if (ApplicationSettings.LOG_ACTIONS) {
      Log.d(LOG_TAG, "performing action: " + action.getName());
    }

    Boolean result = Crash.runComponent(
      "action", action.getName(),
      new Callable<Boolean>() {
        @Override
        public Boolean call () {
          if (action.performAction()) {
            Integer confirmation = action.getConfirmation();
            if (confirmation != null) ApplicationUtilities.message(confirmation);
            return true;
          }

          Log.w(LOG_TAG, "action failed: " + action.getName());
          return false;
        }
      }
    );

    if (result == null) return false;
    return result;
  }

  public static boolean performAction (Action action, int... cursorKeys) {
    for (int key : cursorKeys) pressedCursorKeys.add(key);
    boolean result = performAction(action);
    for (int key : cursorKeys) pressedCursorKeys.remove(key);
    return result;
  }

  private static Action getAction (Class<? extends Action> type, Endpoint endpoint) {
    if (type == null) return null;
    return endpoint.getKeyBindings().getAction(type);
  }

  public static boolean performAction (Class<? extends Action> type, Endpoint endpoint) {
    Action action = getAction(type, endpoint);
    if (action == null) return false;
    return performAction(action);
  }

  private final static Map<Class<? extends Action>, Class<? extends Action>> reversePanningActionMap =
               new HashMap<Class<? extends Action>, Class<? extends Action>>()
  {
    {
      put(PanLeft.class, PanRight.class);
      put(PanRight.class, PanLeft.class);

      put(MoveBackward.class, MoveForward.class);
      put(MoveForward.class, MoveBackward.class);
    }
  };

  private static Action getAction (KeySet keys, boolean isLongPress) {
    Endpoint endpoint = Endpoints.getCurrentEndpoint();
    KeyBindings keyBindings = endpoint.getKeyBindings();
    Action action = null;

    if (isLongPress && ApplicationSettings.LONG_PRESS) {
      int longPress = KeySet.LONG_PRESS;

      keys.add(longPress);
      action = keyBindings.getAction(keys);
      keys.remove(longPress);
    }

    if (action == null) {
      action = keyBindings.getAction(keys);
    }

    if (action == null) {
      if (keys.isDots()) {
        action = keyBindings.getAction(TypeCharacter.class);
      }
    }

    if (action != null) {
      if (ApplicationSettings.REVERSE_PANNING) {
        if (keys.get(KeySet.PAN_FORWARD) != keys.get(KeySet.PAN_BACKWARD)) {
          Action reverse = keyBindings.getAction(reversePanningActionMap.get((Class<? extends Action>)action.getClass()));
          if (reverse != null) action = reverse;
        }
      }
    }

    return action;
  }

  private static boolean performAction (boolean isLongPress) {
    KeySet keys = activeNavigationKeys;
    if (keys.isEmpty()) return true;
    boolean wasModifier = false;

    try {
      Action action = getAction(keys, isLongPress);
      boolean performed = false;

      if (action != null) {
        pressedKeyboardKeys.clear();
        if (action instanceof ModifierAction) wasModifier = true;
        if (!action.isHidden()) Devices.braille.get().dismiss();
        if (performAction(action)) performed = true;
      } else if (handleKeyboardFlush()) {
        performed = true;
      }

      if (!performed) Tones.beep();
      return performed;
    } finally {
      activeNavigationKeys.clear();
      if (!wasModifier) ModifierAction.cancelModifiers();
    }
  }

  private final static Object AWAKEN_LOCK = new Object();
  private static PowerManager.WakeLock keyEventWakeLock = null;

  private static void awakenSystem () {
    synchronized (AWAKEN_LOCK) {
      if (keyEventWakeLock == null) {
        keyEventWakeLock = ApplicationContext.newWakeLock(
          PowerManager.ACQUIRE_CAUSES_WAKEUP |
          PowerManager.ON_AFTER_RELEASE |
          PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
          "key-event"
        );
      }

      keyEventWakeLock.acquire();
      keyEventWakeLock.release();
    }
  }

  private static void onKeyPress () {
    awakenSystem();
  }

  private static Timeout longPressTimeout = new Timeout(ApplicationParameters.LONG_PRESS_TIME, "long-press-timeout") {
    @Override
    public void run () {
      performAction(true);
    }
  };

  public final static void resetKeys () {
    synchronized (longPressTimeout) {
      if (ApplicationSettings.LOG_KEYBOARD) {
        Log.d(LOG_TAG, "resetting key state");
      }

      navigationKeyReleaseTime = 0;
      activeNavigationKeys.clear();
      pressedNavigationKeys.clear();
      pressedCursorKeys.clear();
      pressedKeyboardKeys.clear();

      oneHandNavigationKeyPressed = false;
      oneHandSpaceTimeout = 0;
    }
  }

  static {
    resetKeys();
  }

  public static KeySet getNavigationKeys () {
    return activeNavigationKeys;
  }

  private static void logNavigationKeysChange (int key, String action) {
    if (ApplicationSettings.LOG_KEYBOARD) {
      StringBuilder sb = new StringBuilder();

      sb.append("navigation key ");
      sb.append(action);

      sb.append(": ");
      sb.append(key);

      sb.append(" -> ");
      sb.append(pressedNavigationKeys.toString());

      Log.d(LOG_TAG, sb.toString());
    }
  }

  private static boolean handleEndpointNavigationKeyEvent (int key, boolean press) {
    return Endpoints.getCurrentEndpoint().handleNavigationKeyEvent(key, press);
  }

  private static void handleNavigationKeyPress (int key) {
    onKeyPress();

    synchronized (longPressTimeout) {
      if (pressedNavigationKeys.add(key)) {
        logNavigationKeysChange(key, "press");
        handleKeyboardPress(key);

        if (!handleEndpointNavigationKeyEvent(key, true)) {
          if (ApplicationSettings.ONE_HAND) {
            if (pressedNavigationKeys.size() == 1) {
              if ((navigationKeyReleaseTime + ApplicationSettings.PRESSED_TIMEOUT) < SystemClock.elapsedRealtime()) {
                activeNavigationKeys.clear();
              }
            }

            if (key != oneHandCompletionKey) activeNavigationKeys.add(key);
            oneHandNavigationKeyPressed = true;
          } else {
            activeNavigationKeys.set(pressedNavigationKeys);
            longPressTimeout.start();
          }
        }
      }
    }
  }

  private static void handleNavigationKeyRelease (int key) {
    synchronized (longPressTimeout) {
      if (pressedNavigationKeys.get(key)) {
        try {
          if (!handleEndpointNavigationKeyEvent(key, false)) {
            longPressTimeout.cancel();

            long now = SystemClock.elapsedRealtime();
            navigationKeyReleaseTime = now;

            boolean isComplete = !ApplicationSettings.ONE_HAND
                              || activeNavigationKeys.intersects(oneHandImmediateKeys)
                               ;

            if (oneHandNavigationKeyPressed) {
              // first key release of a fully pressed combination
              oneHandNavigationKeyPressed = false;

              // capture and reset the space timeout so that only one space can be quick
              long spaceTimeout = oneHandSpaceTimeout;
              oneHandSpaceTimeout = 0;

              if (pressedNavigationKeys.get(oneHandCompletionKey)) {
                // the combination included Space

                pressedNavigationKeys.remove(oneHandCompletionKey);
                boolean otherKeysPressed = !pressedNavigationKeys.isEmpty();
                pressedNavigationKeys.add(oneHandCompletionKey);

                if (otherKeysPressed) {
                  // the combination also included at least one other key
                  activeNavigationKeys.add(oneHandCompletionKey);
                  isComplete = true;
                } else if (activeNavigationKeys.isEmpty()) {
                  // it's an initial Space so start a new combination with it
                  activeNavigationKeys.add(oneHandCompletionKey);
                  if (SystemClock.elapsedRealtime() < spaceTimeout) isComplete = true;
                } else {
                  // just Space was pressed so complete the pending combination
                  isComplete = true;

                  if (!activeNavigationKeys.equals(oneHandCompletionKeySet)) {
                    // start the quick space timeout except after Space itself
                    oneHandSpaceTimeout = now + ApplicationSettings.SPACE_TIMEOUT;
                  }
                }
              }
            }

            if (isComplete) performAction(false);
          }
        } finally {
          handleKeyboardRelease(key);
          pressedNavigationKeys.remove(key);
          logNavigationKeysChange(key, "release");
        }
      }
    }
  }

  public static void handleNavigationKeyEvent (int key, boolean press) {
    if (press) {
      handleNavigationKeyPress(key);
    } else {
      handleNavigationKeyRelease(key);
    }
  }

  public static void handleNavigationKey (int key) {
    handleNavigationKeyPress(key);
    handleNavigationKeyRelease(key);
  }

  public static int[] getCursorKeys () {
    int[] keyNumbers = new int[pressedCursorKeys.size()];
    int index = 0;

    for (int keyNumber : pressedCursorKeys) {
      keyNumbers[index++] = keyNumber;
    }

    return keyNumbers;
  }

  private static void logCursorKeyAction (int keyNumber, String action) {
    if (ApplicationSettings.LOG_KEYBOARD) {
      StringBuilder sb = new StringBuilder();

      sb.append("cursor key ");
      sb.append(action);

      sb.append(": ");
      sb.append(keyNumber);

      sb.append(" (");
      for (Integer key : pressedCursorKeys) {
        if (sb.length() > 0) sb.append(", ");;
        sb.append(key);
      }
      sb.append(')');

      Log.d(LOG_TAG, sb.toString());
    }
  }

  private static boolean handleEndpointCursorKeyEvent (int key, boolean press) {
    return Endpoints.getCurrentEndpoint().handleCursorKeyEvent(key, press);
  }

  private static void handleCursorKeyPress (int key) {
    onKeyPress();

    synchronized (longPressTimeout) {
      if (pressedCursorKeys.add(key)) {
        logCursorKeyAction(key, "press");

        if (!handleEndpointCursorKeyEvent(key, true)) {
          if (pressedCursorKeys.size() == 1) {
            handleNavigationKey(KeySet.CURSOR);
          }
        }
      }
    }
  }

  private static void handleCursorKeyRelease (int key) {
    synchronized (longPressTimeout) {
      if (pressedCursorKeys.contains(key)) {
        try {
          handleEndpointCursorKeyEvent(key, false);
        } finally {
          pressedCursorKeys.remove(key);
          logCursorKeyAction(key, "release");
        }
      }
    }
  }

  public static void handleCursorKeyEvent (int key, boolean press) {
    if (press) {
      handleCursorKeyPress(key);
    } else {
      handleCursorKeyRelease(key);
    }
  }
}
