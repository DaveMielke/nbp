package org.nbp.b2g.ui.controls;
import org.nbp.b2g.ui.*;

public class LogGesturesControl extends BooleanControl {
  @Override
  public int getLabel () {
    return R.string.LogGestures_control_label;
  }

  @Override
  protected String getPreferenceKey () {
    return "log-gestures";
  }

  @Override
  protected boolean getBooleanDefault () {
    return ApplicationDefaults.LOG_GESTURES;
  }

  @Override
  public boolean getBooleanValue () {
    return ApplicationSettings.LOG_GESTURES;
  }

  @Override
  protected boolean setBooleanValue (boolean value) {
    ApplicationSettings.LOG_GESTURES = value;
    return true;
  }

  public LogGesturesControl () {
    super(ControlGroup.DEVELOPER);
  }
}
