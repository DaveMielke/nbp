package org.nbp.navigator.controls;
import org.nbp.navigator.*;

import org.nbp.common.controls.EnumerationControl;

public class AngleUnitControl extends EnumerationControl<AngleUnit> {
  @Override
  protected int getResourceForLabel () {
    return R.string.control_label_AngleUnit;
  }

  @Override
  protected int getResourceForGroup () {
    return R.string.control_group_units;
  }

  @Override
  protected String getPreferenceKey () {
    return "angle-unit";
  }

  @Override
  protected AngleUnit getEnumerationDefault () {
    return ApplicationDefaults.ANGLE_UNIT;
  }

  @Override
  public AngleUnit getEnumerationValue () {
    return ApplicationSettings.ANGLE_UNIT;
  }

  @Override
  protected boolean setEnumerationValue (AngleUnit value) {
    ApplicationSettings.ANGLE_UNIT = value;
    return true;
  }

  public AngleUnitControl () {
    super();
  }
}
