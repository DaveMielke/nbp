package org.nbp.common.speech;
import org.nbp.common.*;

import org.nbp.common.controls.LogarithmicFloatControl;

public abstract class RateControl extends LogarithmicFloatControl {
  @Override
  protected int getResourceForNext () {
    return R.string.control_speech_rate_next;
  }

  @Override
  protected int getResourceForPrevious () {
    return R.string.control_speech_rate_previous;
  }

  @Override
  protected final float getLinearScale () {
    return super.getLinearScale() / 2.0f;
  }

  public RateControl () {
    super();
  }
}
