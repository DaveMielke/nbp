package org.nbp.navigator;

import org.nbp.common.speech.TextPlayer;
import org.nbp.common.controls.Control;

public abstract class Announcements extends NavigatorComponent {
  private Announcements () {
    super();
  }

  public static class Announcer extends TextPlayer {
    private Announcer () {
      super();
    }

    @Override
    protected final String getEngineName () {
      return ApplicationSettings.SPEECH_ENGINE;
    }

    private boolean isInitializing = false;

    @Override
    protected final void initializeProperties () {
      boolean wasInitializing = isInitializing;
      isInitializing = true;

      Control.restoreCurrentValues(
        Controls.speechVolume,
        Controls.speechRate,
        Controls.speechPitch,
        Controls.speechBalance
      );

      isInitializing = wasInitializing;
    }
  }

  private final static Object SINGLETON_LOCK = new Object();
  private static Announcer announcer = null;

  public final static Announcer getAnnouncer () {
    synchronized (SINGLETON_LOCK) {
      if (announcer == null) announcer = new Announcer();
      return announcer;
    }
  }

  public final static boolean say (CharSequence text, boolean interrupt) {
    Announcer annoncer = getAnnouncer();

    synchronized (announcer) {
      if (interrupt) {
        if (!announcer.stopSpeaking()) {
          return false;
        }
      }

      return announcer.say(text);
    }
  }

  public final static boolean say (CharSequence text) {
    return say(text, false);
  }

  public final static boolean interrupt () {
    return getAnnouncer().stopSpeaking();
  }

  public final static boolean confirmSetting (int label, CharSequence value) {
    Announcer announcer = getAnnouncer();
    if (announcer.isInitializing) return true;

    StringBuilder confirmation = new StringBuilder();
    confirmation.append(getString(label));
    confirmation.append(' ');
    confirmation.append(value);

    if (!announcer.stopSpeaking()) return false;
    return announcer.say(confirmation.toString());
  }
}
