package org.nbp.b2g.ui.host.actions;
import org.nbp.b2g.ui.host.*;
import org.nbp.b2g.ui.*;

public class ListActions extends Action {
  @Override
  public boolean performAction () {
    ActionChooser.chooseAction(getEndpoint().getRootKeyBindingMap());
    return true;
  }

  public ListActions (Endpoint endpoint) {
    super(endpoint, false);
  }
}
