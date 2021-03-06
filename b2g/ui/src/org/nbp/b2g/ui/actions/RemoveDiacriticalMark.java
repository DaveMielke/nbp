package org.nbp.b2g.ui.actions;
import org.nbp.b2g.ui.*;

import org.nbp.common.UnicodeUtilities;

public class RemoveDiacriticalMark extends DiacriticAction {
  protected final int getChooseMessage () {
    return R.string.popup_select_diacritic_remove;
  }

  private final static Object MAP_LOCK = new Object();
  private static CharacterMap characterMap = null;

  @Override
  protected final CharacterMap getCharacterMap () {
    synchronized (MAP_LOCK) {
      if (characterMap == null) characterMap = new CharacterMap();
    }

    return characterMap;
  }

  @Override
  protected final void makeDiacriticMap (DiacriticMap map, Character base) {
    StringBuilder decomposition = new StringBuilder(UnicodeUtilities.decompose(base));

    if (decomposition.length() > 1) {
      char diacritic = decomposition.charAt(1);
      decomposition.deleteCharAt(1);
      int count = decomposition.length();

      if (count == 1) {
        map.put(diacritic, decomposition.charAt(0));
      } else {
        int index = 0;

        while (true) {
          mapDiacritic(map, diacritic, decomposition.toString());

          if (++index == count) break;
          char next = decomposition.charAt(index);
          decomposition.setCharAt(index, diacritic);
          diacritic = next;
        }
      }
    }
  }

  @Override
  public boolean editsInput () {
    return true;
  }

  public RemoveDiacriticalMark (Endpoint endpoint) {
    super(endpoint);
  }
}
