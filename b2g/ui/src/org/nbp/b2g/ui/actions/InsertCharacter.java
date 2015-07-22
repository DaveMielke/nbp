package org.nbp.b2g.ui.actions;
import org.nbp.b2g.ui.*;

import android.util.Log;

public class InsertCharacter extends Action {
  private final static String LOG_TAG = InsertCharacter.class.getName();

  private boolean insertText (char character) {
    return getEndpoint().insertText(character);
  }

  private boolean insertCharacter (char character) {
    ModifierAction control = (ControlModifier)getAction(ControlModifier.class);

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

    return insertText(character);
  }

  @Override
  public boolean performAction () {
    int keyMask = getNavigationKeys();

    if (ApplicationSettings.BRAILLE_INPUT) {
      Byte dots = KeyMask.toDots(keyMask);

      if (dots == null) {
        Log.w(LOG_TAG, String.format(
          "not a braille character: %s",
          KeyMask.toString(keyMask)
        ));
      } else if (insertText(Braille.toCharacter(dots))) {
        return true;
      }
    } else {
      Character character = Characters.getCharacters().getCharacter(keyMask);

      if (character == null) {
        Log.w(LOG_TAG, String.format(
          "not mapped to a character: %s",
          KeyMask.toString(keyMask)
        ));
      } else if (insertCharacter(character)) {
        return true;
      }
    }

    return false;
  }

  public InsertCharacter (Endpoint endpoint) {
    super(endpoint, false);
  }
}
