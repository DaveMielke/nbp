package org.nbp.b2g.ui;

public class LongPressControl extends BooleanControl {
  @Override
  public CharSequence getLabel () {
    return getString(R.string.LongPress_control_label);
  }

  @Override
  protected String getPreferenceKey () {
    return "long-press";
  }

  @Override
  protected boolean getBooleanDefault () {
    return ApplicationParameters.DEFAULT_LONG_PRESS;
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
    super(false);
  }
}
