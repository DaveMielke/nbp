package org.nbp.b2g.ui.host.actions;
import org.nbp.b2g.ui.host.*;
import org.nbp.b2g.ui.*;

import android.view.inputmethod.InputConnection;
import android.view.KeyEvent;

public class DeleteNext extends InputAction {
  @Override
  public boolean performAction () {
    HostEndpoint endpoint = getHostEndpoint();

    synchronized (endpoint) {
      if (endpoint.isEditable()) {
        InputConnection connection = getInputConnection();

        if (connection != null) {
          if (endpoint.isSelected()) {
            return deleteSelectedText(connection);
          } else {
            return connection.deleteSurroundingText(0, 1);
          }
        }
      }
    }

    return super.performAction();
  }

  @Override
  protected String getScanCode () {
    return "DELETE";
  }

  @Override
  protected int getKeyCode () {
    return KeyEvent.KEYCODE_FORWARD_DEL;
  }

  public DeleteNext (Endpoint endpoint) {
    super(endpoint, false);
  }
}
