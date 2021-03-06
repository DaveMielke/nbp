package org.nbp.editor.menu.review;
import org.nbp.editor.*;
import org.nbp.editor.spans.RevisionSpan;
import org.nbp.editor.spans.PreviewSpan;

public class NextEdit extends MoveAction {
  public NextEdit (EditorActivity editor) {
    super(editor);
  }

  private final boolean moveToNextEdit () {
    return moveToNextPosition(
      findNextSpanSequence(PreviewSpan.class),
      findNextSpanSequence(RevisionSpan.class)
    );
  }

  @Override
  public void performAction () {
    if (!moveToNextEdit()) {
      showMessage(R.string.message_no_next_edit);
    }
  }
}
