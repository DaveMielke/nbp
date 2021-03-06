package org.nbp.b2g.ui;

import org.nbp.common.controls.Control;

public abstract class ConfirmValueAction extends ControlAction<Control> {
  @Override
  public boolean performAction () {
    getControl().confirmValue();
    return true;
  }

  protected ConfirmValueAction (Endpoint endpoint, Control control, boolean isAdvanced) {
    super(endpoint, control, isAdvanced);
  }
}
