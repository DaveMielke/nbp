package org.nbp.b2g.ui;

import java.util.Map;
import java.util.HashMap;

import android.util.Log;
import android.graphics.Rect;

import android.view.accessibility.AccessibilityNodeInfo;

public class BrailleDevice {
  private final static String LOG_TAG = BrailleDevice.class.getName();

  public final static byte DOT_1 = 0X01;
  public final static byte DOT_2 = 0X02;
  public final static byte DOT_3 = 0X04;
  public final static byte DOT_4 = 0X08;
  public final static byte DOT_5 = 0X10;
  public final static byte DOT_6 = 0X20;
  public final static byte DOT_7 = 0X40;
  public final static byte DOT_8 = (byte)0X80;

  private static byte[] brailleCells = null;

  private static String currentText = "";
  private static int currentIndent = 0;

  private static AccessibilityNodeInfo currentNode = null;
  private static boolean currentDescribe = false;

  private static Map<Character, Byte> characterMap = new HashMap<Character, Byte>();

  public native static boolean openDevice ();
  public native static void closeDevice ();

  public native static String getVersion ();
  public native static int getCellCount ();

  public native static boolean clearCells ();
  public native static boolean writeCells (byte[] cells);

  public static boolean setCharacter (char character, byte dots) {
    characterMap.put(character, dots);
    return true;
  }

  public static boolean setCharacter (char character, int keyMask) {
    if (keyMask == 0) return false;
    if (keyMask == KeyMask.SPACE) keyMask = 0;
    if ((keyMask & ~KeyMask.DOTS_ALL) != 0) return false;

    byte dots = 0;
    if ((keyMask & KeyMask.DOT_1) != 0) dots |= BrailleDevice.DOT_1;
    if ((keyMask & KeyMask.DOT_2) != 0) dots |= BrailleDevice.DOT_2;
    if ((keyMask & KeyMask.DOT_3) != 0) dots |= BrailleDevice.DOT_3;
    if ((keyMask & KeyMask.DOT_4) != 0) dots |= BrailleDevice.DOT_4;
    if ((keyMask & KeyMask.DOT_5) != 0) dots |= BrailleDevice.DOT_5;
    if ((keyMask & KeyMask.DOT_6) != 0) dots |= BrailleDevice.DOT_6;
    if ((keyMask & KeyMask.DOT_7) != 0) dots |= BrailleDevice.DOT_7;
    if ((keyMask & KeyMask.DOT_8) != 0) dots |= BrailleDevice.DOT_8;

    return setCharacter(character, dots);
  }

  private static boolean open () {
    if (brailleCells != null) return true;

    if (openDevice()) {
      int cellCount = getCellCount();

      if (cellCount > 0) {
        brailleCells = new byte[cellCount];
        return true;
      }

      closeDevice();
    }

    return false;
  }

  private static boolean write () {
    if (open()) {
      {
        int length = currentText.length();
        int count = length - currentIndent;
        if (count > brailleCells.length) count = brailleCells.length;
        int toIndex = 0;

        while (toIndex < count) {
          int fromIndex = toIndex + currentIndent;
          char character = (fromIndex < length)? currentText.charAt(fromIndex): ' ';
          Byte dots = characterMap.get(character);
          brailleCells[toIndex++] = (dots != null)? dots: ApplicationParameters.BRAILLE_CHARACTER_UNDEFINED;
        }

        while (toIndex < brailleCells.length) brailleCells[toIndex++] = 0;
      }

      if (currentNode != null) {
        if (ScreenUtilities.isEditable(currentNode)) {
          InputService service = InputService.getInputService();

          if (service != null) {
            int start = service.getSelectionStart();
            int end = service.getSelectionEnd();

            if ((start != InputService.NO_SELECTION) && (end != InputService.NO_SELECTION)) {
              if ((start -= currentIndent) < 0) start = 0;
              if ((end -= currentIndent) > brailleCells.length) end = brailleCells.length;

              while (start < end) {
                brailleCells[start++] = ApplicationParameters.BRAILLE_OVERLAY_SELECTED;
              }

              if ((end >= 0) && (end < brailleCells.length)) {
                brailleCells[end] |= ApplicationParameters.BRAILLE_OVERLAY_CURSOR;
              }
            }
          }
        }
      }

      if (writeCells(brailleCells)) return true;
    }

    return false;
  }

  private static void reset () {
    currentText = null;
    currentIndent = 0;
    currentDescribe = false;

    if (currentNode != null) {
      currentNode.recycle();
      currentNode = null;
    }
  }

  public static boolean write (String text) {
    reset();
    currentText = text;
    return write();
  }

  private static String getClassName (AccessibilityNodeInfo node) {
    String name = node.getClassName().toString();
    int index = name.lastIndexOf('.');
    return name.substring(index+1);
  }

  public static boolean write (AccessibilityNodeInfo node, boolean describe) {
    StringBuilder sb = new StringBuilder();
    String string;
    CharSequence characters;

    if (describe) {
      sb.append(getClassName(node));

      if ((characters = node.getText()) != null) {
        sb.append(" \"");
        sb.append(characters);
        sb.append('"');
      }

      if ((characters = node.getContentDescription()) != null) {
        sb.append(" (");
        sb.append(characters);
        sb.append(')');
      }

      sb.append(' ');
      if (node.isFocusable()) sb.append('i');
      if (node.isScrollable()) sb.append('s');
      if (node.isCheckable()) sb.append('c');
      if (node.isVisibleToUser()) sb.append('V');
      if (node.isEnabled()) sb.append('E');
      if (node.isFocused()) sb.append('I');
      if (node.isAccessibilityFocused()) sb.append('A');
      if (node.isSelected()) sb.append('X');
      if (node.isChecked()) sb.append('C');

      {
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);

        sb.append(' ');
        sb.append(bounds.toShortString());
      }
    } else {
      if (node.isCheckable()) {
        sb.append('[');
        sb.append(node.isChecked()? 'X': ' ');
        sb.append("] ");
      }

      if ((characters = node.getText()) != null) {
        sb.append(characters);
      } else if ((characters = node.getContentDescription()) != null) {
        sb.append(characters);
      } else {
        sb.append('(');
        sb.append(getClassName(node));
        sb.append(')');
      }
    }

    string = sb.toString();
    if ((currentNode == null) || !currentNode.equals(node)) {
      reset();
      currentNode = AccessibilityNodeInfo.obtain(node);
    }

    currentText = string;
    currentDescribe = describe;
    return write();
  }

  public static boolean write (AccessibilityNodeInfo node) {
    return write(node, currentDescribe);
  }

  public static boolean moveLeft () {
    if (currentIndent == 0) return false;

    int length = currentText.length();
    if (currentIndent > length) currentIndent = length;

    if ((currentIndent -= brailleCells.length) < 0) currentIndent = 0;
    return write();
  }

  public static boolean moveRight () {
    int length = currentText.length();
    if ((currentIndent + brailleCells.length) >= length) return false;

    currentIndent += brailleCells.length;
    return write();
  }

  private BrailleDevice () {
  }

  static {
    System.loadLibrary("UserInterface");

    if (open()) {
      Log.d(LOG_TAG, "braille cell count: " + brailleCells.length);

      String version = getVersion();
      Log.d(LOG_TAG, "braille device version: " + version);

      clearCells();
      KeyBindings.load();
    }
  }
}
