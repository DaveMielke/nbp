package org.nbp.b2g.ui;

import java.lang.reflect.*;

import java.util.Map;
import java.util.HashMap;

import android.util.Log;

public class Characters {
  private final static String LOG_TAG = Characters.class.getName();

  public final static char CHAR_NUL   = 0X0000;
  public final static char CHAR_SOH   = 0X0001;
  public final static char CHAR_STX   = 0X0002;
  public final static char CHAR_ETX   = 0X0003;
  public final static char CHAR_EOT   = 0X0004;
  public final static char CHAR_ENQ   = 0X0005;
  public final static char CHAR_ACK   = 0X0006;
  public final static char CHAR_BEL   = 0X0007;
  public final static char CHAR_BS    = 0X0008;
  public final static char CHAR_HT    = 0X0009;
  public final static char CHAR_LF    = 0X000A;
  public final static char CHAR_VT    = 0X000B;
  public final static char CHAR_FF    = 0X000C;
  public final static char CHAR_CR    = 0X000D;
  public final static char CHAR_SO    = 0X000E;
  public final static char CHAR_SI    = 0X000F;
  public final static char CHAR_DLE   = 0X0010;
  public final static char CHAR_DC1   = 0X0011;
  public final static char CHAR_DC2   = 0X0012;
  public final static char CHAR_DC3   = 0X0013;
  public final static char CHAR_DC4   = 0X0014;
  public final static char CHAR_NAK   = 0X0015;
  public final static char CHAR_SYN   = 0X0016;
  public final static char CHAR_ETB   = 0X0017;
  public final static char CHAR_CAN   = 0X0018;
  public final static char CHAR_EM    = 0X0019;
  public final static char CHAR_SUB   = 0X001A;
  public final static char CHAR_ESC   = 0X001B;
  public final static char CHAR_FS    = 0X001C;
  public final static char CHAR_GS    = 0X001D;
  public final static char CHAR_RS    = 0X001E;
  public final static char CHAR_US    = 0X001F;
  public final static char CHAR_SPACE = 0X0020;
  public final static char CHAR_DEL   = 0X007F;

  public static Character getCharacter (String name) {
    name = "CHAR_" + name.toUpperCase();

    try {
      Field field = Characters.class.getField(name);
      int modifiers = field.getModifiers();

      if (Modifier.isStatic(modifiers)) {
        if (Modifier.isFinal(modifiers)) {
          Class type = field.getType();

          if (type.equals(char.class)) {
            return field.getChar(null);
          } else {
            Log.w(LOG_TAG, "field is not a char: " + name);
          }
        } else {
          Log.w(LOG_TAG, "field is not final: " + name);
        }
      } else {
        Log.w(LOG_TAG, "field is not static: " + name);
      }
    } catch (NoSuchFieldException exception) {
    } catch (IllegalAccessException exception) {
      Log.w(LOG_TAG, "field not accessible: " + name);
    }

    return null;
  }

  private final static Characters characters = new Characters(
    "nabcc", "control", "symbols", "spanish"
  );

  public static Characters getCharacters () {
    return characters;
  }

  private final Map<Integer, Character> characterMap = new HashMap<Integer, Character>();
  private final Map<Character, Byte> dotsMap = new HashMap<Character, Byte>();

  public Character getCharacter (int keyMask) {
    return characterMap.get(keyMask);
  }

  public Byte getDots (char character) {
    {
      Byte dots = dotsMap.get(character);
      if (dots != null) return dots;
    }

    {
      Character.UnicodeBlock block = Character.UnicodeBlock.of(character);

      if (block.equals(Character.UnicodeBlock.BRAILLE_PATTERNS)) {
        byte dots = 0;

        if ((character & Braille.UNICODE_DOT_1) != 0) dots |= BrailleDevice.DOT_1;
        if ((character & Braille.UNICODE_DOT_2) != 0) dots |= BrailleDevice.DOT_2;
        if ((character & Braille.UNICODE_DOT_3) != 0) dots |= BrailleDevice.DOT_3;
        if ((character & Braille.UNICODE_DOT_4) != 0) dots |= BrailleDevice.DOT_4;
        if ((character & Braille.UNICODE_DOT_5) != 0) dots |= BrailleDevice.DOT_5;
        if ((character & Braille.UNICODE_DOT_6) != 0) dots |= BrailleDevice.DOT_6;
        if ((character & Braille.UNICODE_DOT_7) != 0) dots |= BrailleDevice.DOT_7;
        if ((character & Braille.UNICODE_DOT_8) != 0) dots |= BrailleDevice.DOT_8;

        return dots;
      }
    }

    return null;
  }

  private static Integer parseKeys (String operand) {
    int length = operand.length();
    int mask = 0;

    for (int index=0; index<length; index+=1) {
      char character = operand.charAt(index);
      Integer bit = KeyMask.toBit(Character.toUpperCase(character));

      if (bit == null) {
        Log.w(LOG_TAG, "unknown key: " + character);
        return null;
      }

      if ((mask & bit) != 0) {
        Log.w(LOG_TAG, "key specified more than once: " + operand);
        return null;
      }

      mask |= bit;
    }

    return mask;
  }

  private static Character parseCharacter (String operand) {
    {
      Character character = getCharacter(operand);
      if (character != null) return character;
    }

    if (operand.length() == 1) return operand.charAt(0);
    Log.w(LOG_TAG, "unknown character: " + operand);
    return null;
  }

  private boolean handleCharDirective (String[] operands) {
    int index = 0;

    if (index == operands.length) {
      Log.w(LOG_TAG, "character not specified");
      return true;
    }

    String characterOperand = operands[index++];
    Character character = parseCharacter(characterOperand);
    if (character == null) return true;

    if (index == operands.length) {
      Log.w(LOG_TAG, "keys not specified");
      return true;
    }

    String keysOperand = operands[index++];
    Integer keyMask = parseKeys(keysOperand);
    if (keyMask == null) return true;
    Byte dots = KeyMask.toDots(keyMask);

    if (dots == null) {
      Log.w(LOG_TAG, "not space or just dots: " + keysOperand);
      return true;
    }

    if (index < operands.length) {
      Log.w(LOG_TAG, "too many operands");
    }

    if (characterMap.get(keyMask) != null) {
      Log.w(LOG_TAG, "key combination already bound: " + keysOperand);
      return true;
    }

    if (dotsMap.get(character) != null) {
      Log.w(LOG_TAG, "character already defined: " + characterOperand);
      return true;
    }

    characterMap.put(keyMask, character);
    dotsMap.put(character, dots);
    return true;
  }

  private InputProcessor makeInputProcessor () {
    DirectiveProcessor directiveProcessor = new DirectiveProcessor();

    directiveProcessor.addDirective("char", new DirectiveProcessor.DirectiveHandler() {
      @Override
      public boolean handleDirective (String[] operands) {
        return handleCharDirective(operands);
      }
    });

    return directiveProcessor;
  }

  public Characters (String... names) {
    InputProcessor inputProcessor = makeInputProcessor();

    for (String name : names) {
      Log.d(LOG_TAG, "begin character definitions: " + name);
      inputProcessor.processInput((name + ".chars"));
      Log.d(LOG_TAG, "end character definitions: " + name);
    }
  }
}
