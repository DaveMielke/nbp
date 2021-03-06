package org.nbp.b2g.ui.actions;
import org.nbp.b2g.ui.*;

import org.liblouis.TranslationBuilder;
import org.liblouis.BrailleTranslation;

public class ShowUncontracted extends CursorKeyAction {
  @Override
  protected final boolean performCursorKeyAction (Endpoint endpoint, int offset) {
    CharSequence text = endpoint.getLineText();

    int from = offset;
    if (endpoint.isWordBreak(from)) return false;

    while (from > 0) {
      if (endpoint.isWordBreak(--from)) {
        from += 1;
        break;
      }
    }

    int length = text.length();
    int to = offset;

    while (++to < length) {
      if (endpoint.isWordBreak(to)) break;
    }

    text = text.subSequence(from, to);
    TranslationBuilder builder = TranslationUtilities.newTranslationBuilder(text);
    builder.setNoContractions(true);

    BrailleTranslation translation = new BrailleTranslation(builder);
    return Endpoints.setPopupEndpoint(translation.getBrailleAsString());
  }

  public ShowUncontracted (Endpoint endpoint) {
    super(endpoint, false);
  }
}
