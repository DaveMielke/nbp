package org.nbp.b2g.ui.actions;
import org.nbp.b2g.ui.*;

public class SpeakFaster extends SpeechAction {
  @Override
  public boolean performAction () {
    SpeechDevice speech = getSpeechDevice();

    synchronized (speech) {
      if (Controls.getRateControl().next()) {
        return true;
      }
    }

    return false;
  }

  public SpeakFaster (Endpoint endpoint) {
    super(endpoint);
  }
}
