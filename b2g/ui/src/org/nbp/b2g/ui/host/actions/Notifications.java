package org.nbp.b2g.ui.host.actions;
import org.nbp.b2g.ui.host.*;
import org.nbp.b2g.ui.*;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;

public class Notifications extends GlobalAction {
  @Override
  protected int getGlobalAction () {
    return AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS;
  }

  @Override
  protected int getKeyCode () {
    return KeyEvent.KEYCODE_NOTIFICATION;
  }

  public Notifications (Endpoint endpoint) {
    super(endpoint, false);
  }
}
