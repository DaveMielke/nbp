package org.nbp.editor.menu.revisions;
import org.nbp.editor.*;

import android.view.MenuItem;
import android.content.DialogInterface;

public class AcceptRevision extends EditorAction {
  public AcceptRevision () {
    super();
  }

  @Override
  public void performAction (final EditorActivity editor, MenuItem item) {
    final EditArea editArea = editor.getEditArea();
    final RevisionSpan revision = editArea.getRevisionSpan();

    if (revision == null) {
      editor.showMessage(R.string.message_original_text);
    } else if (editor.verifyWritableText()) {
      editor.showDialog(
        R.string.menu_revisions_AcceptRevision, R.layout.revision_show,
        revision, R.string.action_accept,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick (DialogInterface dialog, int button) {
            editor.runProtectedOperation(
              new Runnable() {
                @Override
                public void run () {
                  int position = Markup.acceptRevision(editArea.getText(), revision);
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
