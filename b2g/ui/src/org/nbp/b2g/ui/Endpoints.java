package org.nbp.b2g.ui;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.nbp.common.LazyInstantiator;

import org.nbp.b2g.ui.host.HostEndpoint;
import org.nbp.b2g.ui.popup.PopupEndpoint;
import org.nbp.b2g.ui.prompt.FindEndpoint;
import org.nbp.b2g.ui.prompt.UnicodeEndpoint;
import org.nbp.b2g.ui.display.DisplayEndpoint;

public abstract class Endpoints {
  private final static ReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();
  public final static Lock READ_LOCK = READ_WRITE_LOCK.readLock();
  private final static Lock WRITE_LOCK = READ_WRITE_LOCK.writeLock();
  private static Endpoint currentEndpoint = null;

  public static Endpoint getCurrentEndpoint () {
    READ_LOCK.lock();
    try {
      return currentEndpoint;
    } finally {
      READ_LOCK.unlock();
    }
  }

  private static void setCurrentEndpoint (Endpoint endpoint) {
    WRITE_LOCK.lock();
    try {
      if (endpoint != currentEndpoint) {
        if (currentEndpoint != null) currentEndpoint.onBackground();
        currentEndpoint = endpoint;
        if (currentEndpoint != null) currentEndpoint.onForeground();
      }
    } finally {
      WRITE_LOCK.unlock();
    }
  }

  public final static LazyInstantiator<HostEndpoint> host = new
    LazyInstantiator<HostEndpoint>(HostEndpoint.class);

  public final static LazyInstantiator<PopupEndpoint> popup = new
    LazyInstantiator<PopupEndpoint>(PopupEndpoint.class);

  public final static LazyInstantiator<FindEndpoint> find = new
    LazyInstantiator<FindEndpoint>(FindEndpoint.class);

  private final static LazyInstantiator<UnicodeEndpoint> unicode = new
    LazyInstantiator<UnicodeEndpoint>(UnicodeEndpoint.class);

  public final static LazyInstantiator<DisplayEndpoint> display = new
    LazyInstantiator<DisplayEndpoint>(DisplayEndpoint.class);

  public static void setHostEndpoint () {
    setCurrentEndpoint(host.get());
  }

  public static void setPopupEndpoint (CharSequence text, PopupClickHandler handler) {
    PopupEndpoint endpoint = popup.get();
    endpoint.set(text, handler);
    setCurrentEndpoint(endpoint);
  }

  public static void setPopupEndpoint (String text) {
    setPopupEndpoint(text, null);
  }

  public static void setFindEndpoint () {
    setCurrentEndpoint(find.get());
  }

  public static void setUnicodeEndpoint () {
    setCurrentEndpoint(unicode.get());
  }

  public static void setDisplayEndpoint () {
    setCurrentEndpoint(display.get());
  }

  private Endpoints () {
  }

  static {
    setHostEndpoint();
  }
}
