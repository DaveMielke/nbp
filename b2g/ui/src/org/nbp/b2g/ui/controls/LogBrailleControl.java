package org.nbp.b2g.ui.controls;
import org.nbp.b2g.ui.*;

import org.nbp.common.controls.BooleanControl;

public class LogBrailleControl extends BooleanControl {
  @Override
  protected int getResourceForLabel () {
    return R.string.control_label_LogBraille;
  }

  @Override
  protected int getResourceForGroup () {
    return R.string.control_group_developer;
  }

  @Override
  protected String getPreferenceKey () {
    return "log-braille";
  }

  @Override
  protected boolean getBooleanDefault () {
    return ApplicationDefaults.LOG_BRAILLE;
  }

  @Override
  public boolean getBooleanValue () {
    return ApplicationSettings.LOG_BRAILLE;
  }

  @Override
  protected boolean setBooleanValue (boolean value) {
    ApplicationSettings.LOG_BRAILLE = value;
    return true;
  }

  public LogBrailleControl () {
    super();
  }
}
