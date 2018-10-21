package org.nbp.ipaws;

import android.speech.tts.TextToSpeech;

public abstract class ApplicationDefaults {
  private ApplicationDefaults () {
  }

  public final static boolean ALERT_MONITOR = true;
  public final static boolean SPEAK_ALERTS = true;
  public final static TextToSpeech.EngineInfo SPEECH_ENGINE = null;

  public final static String PRIMARY_SERVER = "";
  public final static String SECONDARY_SERVER = "echozio.ca";
}
