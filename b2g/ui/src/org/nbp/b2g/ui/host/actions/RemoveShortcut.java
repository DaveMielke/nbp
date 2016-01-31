package org.nbp.b2g.ui.host.actions;
import org.nbp.b2g.ui.host.*;
import org.nbp.b2g.ui.*;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ActivityInfo;

public class RemoveShortcut extends ShortcutAction {
  @Override
  protected void performShortcutAction (PackageManager pm, ActivityInfo activity) {
    Intent intent = new Intent();
    intent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");

    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, newActivityIntent(activity));
    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getLabel(pm, activity));

    sendIntent(intent);
  }

  public RemoveShortcut (Endpoint endpoint) {
    super(endpoint);
  }
}
