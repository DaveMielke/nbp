package org.nbp.b2g.ui.controls;
import org.nbp.b2g.ui.*;

import org.nbp.common.controls.EnumerationControl;

public class TypingModeControl extends EnumerationControl<TypingMode> {
  @Override
  protected int getResourceForLabel () {
    return R.string.control_label_TypingMode;
  }

  @Override
  protected int getResourceForGroup () {
    return R.string.control_group_input;
  }

  @Override
  protected String getPreferenceKey () {
    return "typing-mode";
  }

  @Override
  protected TypingMode getEnumerationDefault () {
    return ApplicationDefaults.TYPING_MODE;
  }

  @Override
  public TypingMode getEnumerationValue () {
    return ApplicationSettings.TYPING_MODE;
  }

  @Override
  protected boolean setEnumerationValue (TypingMode value) {
    ApplicationSettings.TYPING_MODE = value;
    return true;
  }

  public TypingModeControl () {
    super();
  }
}
