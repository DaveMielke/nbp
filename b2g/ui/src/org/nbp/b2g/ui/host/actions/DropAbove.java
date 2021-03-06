package org.nbp.b2g.ui.host.actions;
import org.nbp.b2g.ui.host.*;
import org.nbp.b2g.ui.*;

public class DropAbove extends DragAction {
  @Override
  public boolean performAction () {
    if (haveFromRegion()) {
      if (dropAbove(getRegion())) {
        return true;
      }
    }

    return false;
  }

  public DropAbove (Endpoint endpoint) {
    super(endpoint, false);
  }
}
