package org.nbp.b2g.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import java.util.Set;
import java.util.LinkedHashSet;

import java.util.Map;
import java.util.HashMap;

import org.nbp.common.Braille;

import android.util.Log;
import android.view.KeyEvent;

public class KeySet {
  private final static String LOG_TAG = KeySet.class.getName();

  private final Set<Integer> keyCodes = new LinkedHashSet<Integer>();
  private boolean hasBeenFrozen = false;

  @Override
  public final int hashCode () {
    return keyCodes.hashCode();
  }

  @Override
  public final boolean equals (Object object) {
    if (object == this) return true;
    if (!(object instanceof KeySet)) return false;
    return keyCodes.equals(((KeySet)object).keyCodes);
  }

  public final int size () {
    return keyCodes.size();
  }

  public final boolean isEmpty () {
    return keyCodes.isEmpty();
  }

  public final Integer[] get () {
    return keyCodes.toArray(new Integer[size()]);
  }

  public final boolean get (Integer code) {
    return keyCodes.contains(code);
  }

  public final boolean intersects (int... codes) {
    for (int code : codes) {
      if (get(code)) return true;
    }

    return false;
  }

  public final boolean intersects (Collection<Integer> codes) {
    for (Integer code : codes) {
      if (get(code)) return true;
    }

    return false;
  }

  public final boolean intersects (KeySet keys) {
    return intersects(keys.keyCodes);
  }

  private final void freezeCheck () {
    if (hasBeenFrozen) throw new IllegalStateException("has been frozen");
  }

  public final KeySet freeze () {
    freezeCheck();
    hasBeenFrozen = true;
    return this;
  }

  public final void clear () {
    freezeCheck();
    keyCodes.clear();
  }

  public final boolean remove (Integer... codes) {
    freezeCheck();
    boolean changed = false;

    for (Integer code : codes) {
      if (keyCodes.remove(code)) changed = true;
    }

    return changed;
  }

  public final boolean add (Integer... codes) {
    freezeCheck();
    boolean changed = false;

    for (Integer code : codes) {
      if (keyCodes.add(code)) changed = true;
    }

    return changed;
  }

  public final boolean add (Collection<Integer> codes) {
    freezeCheck();
    return keyCodes.addAll(codes);
  }

  public final boolean add (KeySet keys) {
    return add(keys.keyCodes);
  }

  public final void set (Integer... codes) {
    clear();
    add(codes);
  }

  public final void set (Collection<Integer> codes) {
    clear();
    add(codes);
  }

  public final void set (KeySet keys) {
    set(keys.keyCodes);
  }

  public KeySet (Integer... codes) {
    add(codes);
  }

  public KeySet (Collection<Integer> codes) {
    add(codes);
  }

  public KeySet (KeySet keys) {
    add(keys);
  }

  private static String normalizeKeyName (String name) {
    return name.toLowerCase();
  }

  private static class KeyDefinition {
    private final int sortOrder;
    private final int keyCode;
    private final String keyName;

    public KeyDefinition (int order, int code, String name) {
      sortOrder = order;
      keyCode = code;
      keyName = name;
    }

    public final int getOrder () {
      return sortOrder;
    }

    public final int getCode () {
      return keyCode;
    }

    public final String getName () {
      return keyName;
    }
  }

  private final static Map<String, KeyDefinition> nameToKey =
               new HashMap<String, KeyDefinition>();

  private final static Map<Integer, KeyDefinition> codeToKey =
               new HashMap<Integer, KeyDefinition>();

  private static KeyDefinition addKey (Integer code, String name) {
    synchronized (nameToKey) {
      synchronized (codeToKey) {
        int order = nameToKey.size();
        KeyDefinition key = new KeyDefinition(order, code, name);

        String normalizedName = normalizeKeyName(name);
        KeyDefinition nameKey = nameToKey.put(normalizedName, key);

        if (nameKey == null) {
          KeyDefinition codeKey = codeToKey.put(code, key);

          if (codeKey == null) {
            return key;
          } else {
            Log.w(LOG_TAG,
              String.format(
                "key code defined more than once: %d: %s & %s",
                code, codeKey.getName(), name
              )
            );

            codeToKey.put(code, codeKey);
          }
        } else {
          Log.w(LOG_TAG,
            String.format(
              "key name defined more than once: %s: %d & %d",
              name, nameKey.getCode(), code
            )
          );

          nameToKey.put(normalizedName, nameKey);
        }
      }
    }

    return null;
  }

  private final static Object ADD_KEY_LOCK = new Object();
  public final static int maximumKeyboardCode = KeyEvent.getMaxKeyCode();
  private static int lastAssignedCode = maximumKeyboardCode;

  private static int addKey (String name) {
    synchronized (ADD_KEY_LOCK) {
      int code = ++lastAssignedCode;
      addKey(code, name);
      return code;
    }
  }

  private static KeyDefinition addKey (Integer code) {
    if (code < 0) {
      throw new IllegalArgumentException(("negative key code: " + code));
    }

    if (code > maximumKeyboardCode) {
      throw new IllegalArgumentException(
        String.format(
          "key code exceeds range: %d > %d",
          code, maximumKeyboardCode
        )
      );
    }

    synchronized (ADD_KEY_LOCK) {
      String name = KeyEvent.keyCodeToString(code);
      if ((name == null) || name.isEmpty()) name = Integer.toString(code);
      if (Character.isDigit(name.charAt(0))) name = "Key#" + name;

      return addKey(code, name);
    }
  }

  private static void addKeys (int... codes) {
    for (int code : codes) {
      addKey(code);
    }
  }

  public final static int PAN_FORWARD  = addKey("Forward");
  public final static int PAN_BACKWARD = addKey("Backward");
  public final static int SPACE        = addKey("Space");

  public final static int PAD_UP       = addKey("Up");
  public final static int PAD_DOWN     = addKey("Down");
  public final static int PAD_LEFT     = addKey("Left");
  public final static int PAD_RIGHT    = addKey("Right");
  public final static int PAD_CENTER   = addKey("Center");

  public final static int DOT_1        = addKey("Dot1");
  public final static int DOT_2        = addKey("Dot2");
  public final static int DOT_3        = addKey("Dot3");
  public final static int DOT_4        = addKey("Dot4");
  public final static int DOT_5        = addKey("Dot5");
  public final static int DOT_6        = addKey("Dot6");
  public final static int DOT_7        = addKey("Dot7");
  public final static int DOT_8        = addKey("Dot8");

  public final static int VOLUME_DOWN  = addKey("VolumeDown");
  public final static int VOLUME_UP    = addKey("VolumeUp");

  public final static int CURSOR       = addKey("Cursor");
  public final static int LONG_PRESS   = addKey("LongPress");

  protected final static KeySet panKeys = new KeySet(
    PAN_FORWARD, PAN_BACKWARD
  ).freeze();

  public static boolean isPanCode (int code) {
    return panKeys.get(code);
  }

  protected final static KeySet padKeys = new KeySet(
    PAD_UP, PAD_DOWN,
    PAD_LEFT, PAD_RIGHT,
    PAD_CENTER
  ).freeze();

  public static boolean isPadCode (int code) {
    return padKeys.get(code);
  }

  protected final static KeySet dotKeys = new KeySet(
    DOT_1, DOT_2, DOT_3, DOT_4,
    DOT_5, DOT_6, DOT_7, DOT_8
  ).freeze();

  public static boolean isDotCode (int code) {
    return dotKeys.get(code);
  }

  protected final static KeySet volumeKeys = new KeySet(
    VOLUME_DOWN, VOLUME_UP
  ).freeze();

  public static boolean isVolumeCode (int code) {
    return volumeKeys.get(code);
  }

  private final static Map<Integer, Byte> codeToDot =
               new HashMap<Integer, Byte>()
  {
    {
      put(DOT_1, Braille.CELL_DOT_1);
      put(DOT_2, Braille.CELL_DOT_2);
      put(DOT_3, Braille.CELL_DOT_3);
      put(DOT_4, Braille.CELL_DOT_4);
      put(DOT_5, Braille.CELL_DOT_5);
      put(DOT_6, Braille.CELL_DOT_6);
      put(DOT_7, Braille.CELL_DOT_7);
      put(DOT_8, Braille.CELL_DOT_8);
    }
  };

  public final Byte toDots () {
    byte dots = 0;
    boolean space = false;

    for (Integer code : keyCodes) {
      if (code == SPACE) {
        space = true;
      } else {
        Byte dot = codeToDot.get(code);
        if (dot == null) return null;
        dots |= dot;
      }
    }

    if (space == (dots != 0)) return null;
    return dots;
  }

  public final boolean isDots () {
    return toDots() != null;
  }

  public static KeySet fromDots (byte dots) {
    KeySet keys = new KeySet();

    if ((dots & Braille.CELL_DOT_1) != 0) keys.add(DOT_1);
    if ((dots & Braille.CELL_DOT_2) != 0) keys.add(DOT_2);
    if ((dots & Braille.CELL_DOT_3) != 0) keys.add(DOT_3);
    if ((dots & Braille.CELL_DOT_4) != 0) keys.add(DOT_4);
    if ((dots & Braille.CELL_DOT_5) != 0) keys.add(DOT_5);
    if ((dots & Braille.CELL_DOT_6) != 0) keys.add(DOT_6);
    if ((dots & Braille.CELL_DOT_7) != 0) keys.add(DOT_7);
    if ((dots & Braille.CELL_DOT_8) != 0) keys.add(DOT_8);

    if (keys.isEmpty()) keys.add(SPACE);
    return keys.freeze();
  }

  public static KeySet fromDotNumbers (String numbers) {
    Byte dots = Braille.parseDotNumbers(numbers);
    if (dots == null) return null;
    return fromDots(dots);
  }

  public static KeySet fromName (String name) {
    name = normalizeKeyName(name);

    synchronized (nameToKey) {
      KeyDefinition key = nameToKey.get(name);
      if (key != null) return new KeySet(key.getCode()).freeze();
    }

    {
      String prefix = "dots";

      if (name.startsWith(prefix)) {
        KeySet keys = fromDotNumbers(name.substring(prefix.length()));
        if (keys != null) return keys;
      }
    }

    Log.w(LOG_TAG, ("unknown key name: " + name));
    return null;
  }

  public final String toString () {
    StringBuilder sb = new StringBuilder();

    if (!isEmpty()) {
      KeyDefinition[] keys;

      synchronized (codeToKey) {
        keys = new KeyDefinition[keyCodes.size()];
        int index = 0;

        for (Integer code : keyCodes) {
          KeyDefinition key = codeToKey.get(code);
          if (key == null) key = addKey(code);
          keys[index++] = key;
        }
      }

      Arrays.sort(keys,
        new Comparator<KeyDefinition>() {
          @Override
          public int compare (KeyDefinition key1, KeyDefinition key2) {
            return Integer.compare(key1.getOrder(), key2.getOrder());
          }
        }
      );
      int dotCount = 0;

      for (KeyDefinition key : keys) {
        int code = key.getCode();
        String name = key.getName();
        boolean isDot = isDotCode(code);

        if (!(isDot && (dotCount > 0))) {
          if (sb.length() > 0) sb.append(KeyBindings.KEY_NAME_DELIMITER);
          sb.append(name);
        } else {
          if (dotCount == 1) sb.insert((sb.length() - 1), 's');
          sb.append(name.charAt(name.length() - 1));
        }

        dotCount = isDot? (dotCount + 1): 0;
      }
    }

    return sb.toString();
  }

  public static boolean isKeyboardCode (int code) {
    if (code < 0) return false;
    if (code > maximumKeyboardCode) return false;
    return code != KeyEvent.KEYCODE_UNKNOWN;
  }

  private static void addKeyboardCodes () {
    addKeys(
      KeyEvent.KEYCODE_SYSRQ    , KeyEvent.KEYCODE_BREAK      ,
      KeyEvent.KEYCODE_CAPS_LOCK, KeyEvent.KEYCODE_SCROLL_LOCK,
      KeyEvent.KEYCODE_NUM_LOCK ,

      KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT,
      KeyEvent.KEYCODE_CTRL_LEFT , KeyEvent.KEYCODE_CTRL_RIGHT ,
      KeyEvent.KEYCODE_ALT_LEFT  , KeyEvent.KEYCODE_ALT_RIGHT  ,
      KeyEvent.KEYCODE_META_LEFT , KeyEvent.KEYCODE_META_RIGHT ,

      KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_ESCAPE,
      KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_TAB   ,

      KeyEvent.KEYCODE_F1 , KeyEvent.KEYCODE_F2 , KeyEvent.KEYCODE_F3 ,
      KeyEvent.KEYCODE_F4 , KeyEvent.KEYCODE_F5 , KeyEvent.KEYCODE_F6 ,
      KeyEvent.KEYCODE_F7 , KeyEvent.KEYCODE_F8 , KeyEvent.KEYCODE_F9 ,
      KeyEvent.KEYCODE_F10, KeyEvent.KEYCODE_F11, KeyEvent.KEYCODE_F12, 

      KeyEvent.KEYCODE_DPAD_UP    , KeyEvent.KEYCODE_DPAD_DOWN ,
      KeyEvent.KEYCODE_DPAD_LEFT  , KeyEvent.KEYCODE_DPAD_RIGHT,
      KeyEvent.KEYCODE_DPAD_CENTER,

      KeyEvent.KEYCODE_PAGE_UP  , KeyEvent.KEYCODE_PAGE_DOWN  ,
      KeyEvent.KEYCODE_MOVE_HOME, KeyEvent.KEYCODE_MOVE_END   ,
      KeyEvent.KEYCODE_DEL      , KeyEvent.KEYCODE_FORWARD_DEL,
      KeyEvent.KEYCODE_INSERT   ,

      KeyEvent.KEYCODE_NUMPAD_0         , KeyEvent.KEYCODE_NUMPAD_1          ,
      KeyEvent.KEYCODE_NUMPAD_2         , KeyEvent.KEYCODE_NUMPAD_3          ,
      KeyEvent.KEYCODE_NUMPAD_4         , KeyEvent.KEYCODE_NUMPAD_5          ,
      KeyEvent.KEYCODE_NUMPAD_6         , KeyEvent.KEYCODE_NUMPAD_7          ,
      KeyEvent.KEYCODE_NUMPAD_8         , KeyEvent.KEYCODE_NUMPAD_9          ,
      KeyEvent.KEYCODE_NUMPAD_DOT       , KeyEvent.KEYCODE_NUMPAD_COMMA      ,
      KeyEvent.KEYCODE_NUMPAD_ADD       , KeyEvent.KEYCODE_NUMPAD_SUBTRACT   ,
      KeyEvent.KEYCODE_NUMPAD_MULTIPLY  , KeyEvent.KEYCODE_NUMPAD_DIVIDE     ,
      KeyEvent.KEYCODE_NUMPAD_LEFT_PAREN, KeyEvent.KEYCODE_NUMPAD_RIGHT_PAREN,
      KeyEvent.KEYCODE_NUMPAD_ENTER     , KeyEvent.KEYCODE_NUMPAD_EQUALS     ,

      KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_C,
      KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_F,
      KeyEvent.KEYCODE_G, KeyEvent.KEYCODE_H, KeyEvent.KEYCODE_I,
      KeyEvent.KEYCODE_J, KeyEvent.KEYCODE_K, KeyEvent.KEYCODE_L,
      KeyEvent.KEYCODE_M, KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_O,
      KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_R,
      KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_T, KeyEvent.KEYCODE_U,
      KeyEvent.KEYCODE_V, KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_X,
      KeyEvent.KEYCODE_Y, KeyEvent.KEYCODE_Z,

      KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_1,
      KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3,
      KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5,
      KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_7,
      KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_9,

      KeyEvent.KEYCODE_LEFT_BRACKET, KeyEvent.KEYCODE_RIGHT_BRACKET,
      KeyEvent.KEYCODE_MINUS       , KeyEvent.KEYCODE_EQUALS       ,
      KeyEvent.KEYCODE_APOSTROPHE  , KeyEvent.KEYCODE_GRAVE        ,
      KeyEvent.KEYCODE_COMMA       , KeyEvent.KEYCODE_PERIOD       ,
      KeyEvent.KEYCODE_SLASH       , KeyEvent.KEYCODE_BACKSLASH
    );
  }

  static {
    addKeyboardCodes();
  }
}
