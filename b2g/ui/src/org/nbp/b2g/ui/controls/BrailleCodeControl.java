package org.nbp.b2g.ui.controls;
import org.nbp.b2g.ui.*;

import org.nbp.common.controls.EnumerationControl;

import android.util.Log;

public class BrailleCodeControl extends EnumerationControl<BrailleCode> {
  private final static String LOG_TAG = BrailleCodeControl.class.getName();

  @Override
  protected int getResourceForLabel () {
    return R.string.control_label_BrailleCode;
  }

  @Override
  protected int getResourceForGroup () {
    return R.string.control_group_general;
  }

  @Override
  protected String getPreferenceKey () {
    return "braille-code";
  }

  @Override
  protected String getValueLabel (BrailleCode code) {
    String label = code.getDescription();
    if (label == null) label = super.getValueLabel(code);
    return label;
  }

  @Override
  protected boolean testEnumerationValue (BrailleCode value) {
    String problem;

    try {
      if (value.getTranslator() != null) return true;
      problem = "translator not defined";
    } catch (Exception exception) {
      problem = exception.getMessage();
    }

    StringBuilder log = new StringBuilder();
    log.append("braille code not available: ");
    log.append(value.getDescription());
    log.append(": ");
    log.append(problem);
    Log.w(LOG_TAG, log.toString());

    return false;
  }

  @Override
  protected BrailleCode getEnumerationDefault () {
    return ApplicationDefaults.BRAILLE_CODE;
  }

  @Override
  public BrailleCode getEnumerationValue () {
    return ApplicationSettings.BRAILLE_CODE;
  }

  @Override
  protected boolean setEnumerationValue (BrailleCode value) {
    ApplicationSettings.BRAILLE_CODE = value;
    TranslationUtilities.refresh();
    return true;
  }

  public BrailleCodeControl () {
    super();
  }
}
