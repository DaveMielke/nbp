package org.nbp.b2g.ui.host.actions;
import org.nbp.b2g.ui.host.*;
import org.nbp.b2g.ui.*;

import android.content.ClipboardManager;
import android.content.ClipData;

public class PasteFromClipboard extends InputAction {
  @Override
  public boolean performAction () {
    HostEndpoint endpoint = getHostEndpoint();

    synchronized (endpoint) {
      if (endpoint.isEditable()) {
        ClipboardManager clipboard = getClipboard();

        if (clipboard != null) {
          ClipData clip = clipboard.getPrimaryClip();

          if (clip != null) {
            String text = getClipText(clip);

            if (text != null) {
              InputService service = getInputService();

              if (service != null) {
                if (service.insert(text)) {
                  return true;
                }
              }
            }
          }
        }
      }
    }

    return false;
  }

  public PasteFromClipboard (Endpoint endpoint) {
    super(endpoint, false);
  }
}
