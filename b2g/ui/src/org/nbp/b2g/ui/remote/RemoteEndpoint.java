package org.nbp.b2g.ui.remote;
import org.nbp.b2g.ui.*;

public class RemoteEndpoint extends Endpoint {
  private final Channel currentChannel;
  private final Protocol currentProtocol;

  public final Channel getChannel () {
    return currentChannel;
  }

  public final Protocol getProtocol () {
    return currentProtocol;
  }

  public final boolean start () {
    return currentChannel.start();
  }

  public final boolean stop () {
    boolean stopped = currentChannel.stop();
    write("remote display off");
    return stopped;
  }

  @Override
  public void onBackground () {
    try {
      currentProtocol.resetKeys();
    } finally {
      super.onBackground();
    }
  }

  @Override
  public final int handleNavigationKeyEvent (int keyMask, boolean press) {
    return currentProtocol.handleNavigationKeyEvent(keyMask, press);
  }

  @Override
  public final boolean handleCursorKeyEvent (int keyNumber, boolean press) {
    return currentProtocol.handleCursorKeyEvent(keyNumber, press);
  }

  public RemoteEndpoint () {
    super("remote");

    currentChannel = new BluetoothChannel();
    currentProtocol = new BaumProtocol();
  }
}