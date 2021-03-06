package org.nbp.editor;

import android.view.MenuItem;

public abstract class EditorAction extends EditorComponent {
  protected EditorAction (EditorActivity editor) {
    super(editor);
  }

  protected final void performWithoutRegionProtection (Runnable operation) {
    getEditor().performWithoutRegionProtection(operation);
  }

  public void performAction (EditorActivity editor) {
  }

  public void performAction () {
    performAction(getEditor());
  }

  public void performAction (MenuItem item) {
    performAction();
  }
}
