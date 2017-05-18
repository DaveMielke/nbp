package org.nbp.b2g.ui.controls;
import org.nbp.b2g.ui.*;

public class BrailleEnabledControl extends BooleanControl {
  @Override
  public int getLabel () {
    return R.string.control_label_BrailleEnabled;
  }

  @Override
  protected String getPreferenceKey () {
    return "braille-enabled";
  }

  @Override
  protected boolean getBooleanDefault () {
    return ApplicationDefaults.BRAILLE_ENABLED;
  }

  @Override
  public boolean getBooleanValue () {
    return ApplicationSettings.BRAILLE_ENABLED;
  }

  @Override
  protected boolean setBooleanValue (boolean value) {
    BrailleDevice braille = Devices.braille.get();
    if (!(value? braille.enable(): braille.disable())) return false;

    ApplicationSettings.BRAILLE_ENABLED = value;
    if (value) braille.refreshCells();
    return true;
  }

  public BrailleEnabledControl () {
    super(ControlGroup.BRAILLE);
  }
}
