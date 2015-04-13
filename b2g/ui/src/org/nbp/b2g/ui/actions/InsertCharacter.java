package org.nbp.b2g.ui.actions;
import org.nbp.b2g.ui.*;

import android.util.Log;

public class InsertCharacter extends Action {
  private final static String LOG_TAG = InsertCharacter.class.getName();

  private Characters getCharacters () {
    return getEndpoint().getCharacters();
  }

  @Override
  public boolean parseOperand (int keyMask, String operand) {
    return getCharacters().setCharacter(keyMask, operand);
  }

  private boolean insertCharacter (char character) {
    Endpoint endpoint = getEndpoint();
    ModifierAction control = (ControlModifier)endpoint.getKeyBindings().getAction(ControlModifier.class);

    if (control != null) {
      if (control.getState()) {
        if ((character >= 0X40) && (character <= 0X7E)) {
          character &= 0X1F;
        } else if (character == 0X3F) {
          character |= 0X40;
        } else {
          ApplicationUtilities.beep();
          return false;
        }
      }
    }

    return endpoint.insertText(character);
  }

  @Override
  public boolean performAction () {
    int keyMask = getNavigationKeys();
    Character character = getCharacters().getCharacter(keyMask);

    if (character == null) {
      Log.w(LOG_TAG, String.format("not mapped to a character: 0X%02X", keyMask));
    } else if (insertCharacter(character)) {
      return true;
    }

    return false;
  }

  public InsertCharacter (Endpoint endpoint) {
    super(endpoint, false);
  }
}
