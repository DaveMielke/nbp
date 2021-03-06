package org.nbp.editor.menu.revisions;
import org.nbp.editor.*;
import org.nbp.editor.spans.RevisionSpan;

import android.content.DialogInterface;

public class RejectRevision extends RevisionAction {
  public RejectRevision (EditorActivity editor) {
    super(editor);
  }

  @Override
  public void performAction (final EditorActivity editor) {
    final EditArea editArea = editor.getEditArea();
    final RevisionSpan revision = getRevisionSpan();

    if (revision == null) {
      showMessage(R.string.message_original_text);
    } else if (verifyWritableText()) {
      editor.showDialog(
        R.string.menu_revisions_RejectRevision, R.layout.revision_show,
        revision, R.string.action_reject,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick (DialogInterface dialog, int button) {
            performWithoutRegionProtection(
              new Runnable() {
                @Override
                public void run () {
                  int position = Markup.rejectRevision(editArea.getText(), revision);
                  editArea.setSelection(position);
                  editArea.setHasChanged(true);
                }
              }
            );
          }
        }
      );
    }
  }
}
