package org.nbp.b2g.ui.controls;
import org.nbp.b2g.ui.*;

import org.nbp.common.speech.VolumeControl;
import org.nbp.common.speech.SpeechParameters;

public class SpeechVolumeControl extends VolumeControl {
  @Override
  protected int getResourceForLabel () {
    return R.string.control_label_SpeechVolume;
  }

  @Override
  protected int getResourceForGroup () {
    return R.string.control_group_speech;
  }

  @Override
  protected String getPreferenceKey () {
    return "speech-volume";
  }

  @Override
  protected float getFloatDefault () {
    return ApplicationDefaults.SPEECH_VOLUME;
  }

  @Override
  public float getFloatValue () {
    return ApplicationSettings.SPEECH_VOLUME;
  }

  @Override
  protected boolean setFloatValue (float value) {
    if (!SpeechParameters.verifyVolume(value)) return false;

    if (Devices.speech.isInstantiated()) {
      Devices.speech.get().setVolume(value);
    }

    ApplicationSettings.SPEECH_VOLUME = value;
    return true;
  }

  public SpeechVolumeControl () {
    super();
  }
}
