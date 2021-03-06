package org.nbp.editor.menu.highlight;
import org.nbp.editor.*;

import android.text.style.CharacterStyle;
import android.text.style.UnderlineSpan;

public class Underline extends HighlightAction {
  public Underline (EditorActivity editor) {
    super(editor);
  }

  @Override
  public CharacterStyle getCharacterStyle () {
    return new UnderlineSpan();
  }
}
