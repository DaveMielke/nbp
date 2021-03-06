package org.nbp.b2g.ui.actions;
import org.nbp.b2g.ui.*;

public class SayLine extends SayAction {
  @Override
  protected final CharSequence getText (Endpoint endpoint) {
    return endpoint.getLineText();
  }

  public SayLine (Endpoint endpoint) {
    super(endpoint);
  }
}
