package org.nbp.b2g.ui.remote;
import org.nbp.b2g.ui.*;

import android.util.Log;
import android.os.Build;

public abstract class Protocol {
  private final static String LOG_TAG = Protocol.class.getName();

  protected final RemoteEndpoint remoteEndpoint;

  protected Protocol (RemoteEndpoint endpoint) {
    remoteEndpoint = endpoint;
  }

  protected static void logIgnoredByte (byte b) {
    Log.w(LOG_TAG, String.format("input byte ignored: 0X%02X", b));
  }

  public boolean resetInput (boolean timeout) {
    return true;
  }

  public boolean handleInput (byte b) {
    logIgnoredByte(b);
    return true;
  }

  private int cellCount = 0;
  protected final int getCellCount () {
    if (cellCount == 0) cellCount = Devices.braille.get().getLength();
    return cellCount;
  }

  protected final String getString (String string, int width) {
    int length = string.length();
    if (length >= width) return string.substring(length-width);

    StringBuilder sb = new StringBuilder(string);
    while (sb.length() < width) sb.append(' ');
    return sb.toString();
  }

  protected final String getSerialNumber () {
    return Build.SERIAL;
  }

  protected final String getSerialNumber (int width) {
    return getString(getSerialNumber(), width);
  }
}
