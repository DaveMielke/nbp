package org.nbp.b2g.ui;

import java.util.Arrays;

import android.util.Log;
import android.content.Context;

public class BrailleDevice {
  private final static String LOG_TAG = BrailleDevice.class.getName();

  public final static byte DOT_1 =       0X01;
  public final static byte DOT_2 =       0X02;
  public final static byte DOT_3 =       0X04;
  public final static byte DOT_4 =       0X08;
  public final static byte DOT_5 =       0X10;
  public final static byte DOT_6 =       0X20;
  public final static byte DOT_7 =       0X40;
  public final static byte DOT_8 = (byte)0X80;

  private BrailleMonitorWindow monitorWindow = null;

  public BrailleMonitorWindow getMonitorWindow () {
    synchronized (this) {
      if (monitorWindow == null) {
        Context context = ApplicationContext.getContext();
        if (context == null) return null;
        monitorWindow = new BrailleMonitorWindow(context);
      }
    }

    return monitorWindow;
  }

  private native boolean openDevice ();
  private native void closeDevice ();

  private native boolean enableDevice ();
  private native boolean disableDevice ();

  private native String getDriverVersion ();
  private native int getCellCount ();

  private native boolean setCellFirmness (int firmness);

  private native boolean clearCells ();
  private native boolean writeCells (byte[] cells);

  private byte[] brailleCells = null;
  private String brailleText = null;
  private boolean writePending = false;

  public byte[] getCells () {
    synchronized (this) {
      if (!open()) return null;
      return Arrays.copyOf(brailleCells, brailleCells.length);
    }
  }

  public void restoreControls () {
    Control[] controls = new Control[] {
      Controls.getBrailleFirmnessControl(),
      Controls.getBrailleMonitorControl()
    };

    Controls.forEachControl(controls, Controls.restoreCurrentValue);
  }

  public boolean open () {
    synchronized (this) {
      if (brailleCells != null) return true;

      if (openDevice()) {
        int cellCount = getCellCount();

        if (cellCount > 0) {
          brailleCells = new byte[cellCount];
          Log.d(LOG_TAG, "braille cell count: " + brailleCells.length);

          String version = getDriverVersion();
          Log.d(LOG_TAG, "braille driver version: " + version);

          clearCells();
          Braille.clearCells(brailleCells);
          brailleText = "";
          writePending = false;

          restoreControls();
          return true;
        }

        closeDevice();
      }
    }

    return false;
  }

  public void close () {
    synchronized (writeDelay) {
      writeDelay.cancel();

      synchronized (this) {
        writePending = false;
        brailleText = null;

        if (brailleCells != null) {
          brailleCells = null;
          closeDevice();
        }
      }
    }
  }

  public boolean enable () {
    if (open()) {
      if (enableDevice()) {
        return true;
      }
    }

    return false;
  }

  public boolean disable () {
    if (open()) {
      if (disableDevice()) {
        return true;
      }
    }

    return false;
  }

  public int getLength () {
    synchronized (this) {
      if (open()) return brailleCells.length;
      return 0;
    }
  }

  public boolean setFirmness (int firmness) {
    synchronized (this) {
      if (open()) {
        if (setCellFirmness(firmness)) {
          return true;
        }
      }
    }

    return false;
  }

  private void logCells (byte[] cells, String reason, String text) {
    boolean log = ApplicationSettings.LOG_BRAILLE;
    String braille = Braille.toString(cells);

    if (log) {
      Log.d(LOG_TAG, String.format(
        "braille cells: %s: %s", reason, braille
      ));
    }

    if (text != null) {
      BrailleMonitorWindow window = getMonitorWindow();
      if (window != null) window.setContent(braille, text);
      if (log) Log.d(LOG_TAG, "braille text: " + text);
    }
  }

  private void logCells (byte[] cells, String reason) {
    logCells(cells, reason, null);
  }

  private boolean writeCells (byte[] cells, String text, String reason) {
    {
      final int suppliedLength = cells.length;
      final int requiredLength = brailleCells.length;

      if (suppliedLength != requiredLength) {
        byte[] newCells = new byte[requiredLength];
        final int count = Math.min(suppliedLength, requiredLength);

        System.arraycopy(cells, 0, newCells, 0, count);
        Braille.clearCells(newCells, count);

        cells = newCells;
      }
    }

    if (ApplicationSettings.BRAILLE_ENABLED) {
      if (!writeCells(cells)) {
        return false;
      }
    }

    logCells(cells, reason, text);
    return true;
  }

  private boolean writeCells () {
    return writeCells(brailleCells, brailleText, "writing");
  }

  private final Timeout writeDelay = new Timeout(ApplicationParameters.BRAILLE_WRITE_DELAY, "braille-device-write-delay") {
    @Override
    public void run () {
      synchronized (BrailleDevice.this) {
        if (writePending) {
          if (writeCells()) writePending = false;
          start(ApplicationParameters.BRAILLE_REWRITE_DELAY);
        }
      }
    }
  };

  public boolean write () {
    synchronized (this) {
      if (!open()) return false;

      {
        byte[] oldCells = getCells();
        String text = Braille.setCells(brailleCells);

        if (!text.equals(brailleText)) {
          brailleText = text;
        } else if (Arrays.equals(brailleCells, oldCells)) {
          return true;
        }
      }

      writePending = true;
      logCells(brailleCells, "updated");
    }

    synchronized (writeDelay) {
      if (!writeDelay.isActive()) {
        writeDelay.start();
      }
    }

    return true;
  }

  public boolean write (byte[] cells, String text, long duration) {
    synchronized (this) {
      if (open()) {
        writeDelay.cancel();

        if (writeCells(cells, text, "message")) {
          if (duration > 0) {
            writePending = true;
            writeDelay.start(duration);
          }

          return true;
        }
      }
    }

    return false;
  }

  public boolean write (byte[] cells, long duration) {
    return write(cells, "", 0);
  }

  public boolean write (byte[] cells) {
    return write(cells, 0);
  }

  public boolean write (String text, long duration) {
    byte[] cells = new byte[text.length()];
    text = Braille.setCells(cells, text);
    return write(cells, text, duration);
  }

  public boolean write (String text) {
    return write(text, 0);
  }

  public BrailleDevice () {
  }

  static {
    System.loadLibrary("UserInterface");
  }
}
