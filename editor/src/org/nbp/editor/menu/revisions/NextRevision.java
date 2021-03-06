package org.nbp.editor.menu.revisions;
import org.nbp.editor.*;
import org.nbp.editor.spans.RevisionSpan;
import org.nbp.editor.spans.PreviewSpan;

public class NextRevision extends MoveAction {
  public NextRevision (EditorActivity editor) {
    super(editor);
  }

  private final boolean moveToNextRevision () {
    return moveToNextPosition(
      findNextSpan(PreviewSpan.class),
      findNextSpan(RevisionSpan.class)
    );
  }

  @Override
  public void performAction () {
    if (!moveToNextRevision()) {
      showMessage(R.string.message_no_next_revision);
    }
  }
}
