package org.nbp.b2g.ui.controls;
import org.nbp.b2g.ui.*;

public class LongPressControl extends BooleanControl {
  @Override
  public int getLabel () {
    return R.string.control_label_LongPress;
  }

  @Override
  protected String getPreferenceKey () {
    return "long-press";
  }

  @Override
  protected boolean getBooleanDefault () {
    return ApplicationDefaults.LONG_PRESS;
  }

  @Override
  public boolean getBooleanValue () {
    return ApplicationSettings.LONG_PRESS;
  }

  @Override
  protected boolean setBooleanValue (boolean value) {
    ApplicationSettings.LONG_PRESS = value;
    return true;
  }

  public LongPressControl () {
    super(ControlGroup.KEYBOARD);
  }
}
