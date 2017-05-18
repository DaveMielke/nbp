package org.nbp.b2g.ui.controls;
import org.nbp.b2g.ui.*;

public class SpeechPitchControl extends LogarithmicFloatControl {
  @Override
  public int getLabel () {
    return R.string.control_label_SpeechPitch;
  }

  @Override
  public CharSequence getNextLabel () {
    return getString(R.string.control_next_SpeechPitch);
  }

  @Override
  public CharSequence getPreviousLabel () {
    return getString(R.string.control_previous_SpeechPitch);
  }

  @Override
  protected String getPreferenceKey () {
    return "speech-pitch";
  }

  @Override
  protected float getFloatDefault () {
    return ApplicationDefaults.SPEECH_PITCH;
  }

  @Override
  public float getFloatValue () {
    return ApplicationSettings.SPEECH_PITCH;
  }

  @Override
  protected boolean setFloatValue (float value) {
    if (!Devices.speech.get().setPitch(value)) return false;
    ApplicationSettings.SPEECH_PITCH = value;
    return true;
  }

  public SpeechPitchControl () {
    super(ControlGroup.SPEECH);
  }
}
