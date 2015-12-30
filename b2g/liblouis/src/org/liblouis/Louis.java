package org.liblouis;

import android.util.Log;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;

public final class Louis {
  private final static String LOG_TAG = Louis.class.getName();
  private final static String LIBRARY_NAME = "louis";

  private static native String getVersion ();
  private static native void setDataPath (String path);

  private final static String version;

  static {
    System.loadLibrary(LIBRARY_NAME);

    version = getVersion();
    Log.i(LOG_TAG, "liblouis version: " + version);
  }

  private static Context currentContext = null;
  private static File dataDirectory = null;

  private static SharedPreferences getSharedPreferences () {
    return PreferenceManager.getDefaultSharedPreferences(currentContext);
  }

  private static void removeFile (File file, boolean yes) {
    if (file.isDirectory()) {
      file.setWritable(true, true);

      for (String name : file.list()) {
        removeFile(new File(file, name), true);
      }
    }

    if (yes) {
      file.delete();
    }
  }

  private static void extractAssets (AssetManager assets, String asset, File location) {
    try {
      String[] names = assets.list(asset);
      boolean isDirectory = names.length > 0;

      if (isDirectory) {
        if (!location.exists()) {
          location.mkdir();
        } else if (!location.isDirectory()) {
          Log.w(LOG_TAG, "not a directory: " + location.getPath());
          return;
        }

        for (String name : names) {
          extractAssets(assets, new File(asset, name).getPath(), new File(location, name));
        }
      } else {
        InputStream input = assets.open(asset);
        OutputStream output = new FileOutputStream(location);
        byte[] buffer = new byte[0X1000];

        for (int count; ((count = input.read(buffer)) > 0); ) {
          output.write(buffer, 0, count);
        }

        input.close();
        output.close();
      }

      location.setExecutable(isDirectory, false);
      location.setWritable(false, false);
      location.setReadable(true, false);
    } catch (IOException exception) {
      Log.e(LOG_TAG, "directory refresh error: " + exception.getMessage());
    }
  }

  private static void extractAssets () {
    AssetManager assets = currentContext.getAssets();
    File location = dataDirectory;

    removeFile(location, false);
    extractAssets(assets, "liblouis", location);
  }

  private static void updatePackageData () {
    SharedPreferences prefs = getSharedPreferences();
    File file = new File(currentContext.getPackageCodePath());

    String prefKey_size = "package-size";
    long oldSize = prefs.getLong(prefKey_size, -1);
    long newSize = file.length();

    String prefKey_time = "package-time";
    long oldTime = prefs.getLong(prefKey_time, -1);
    long newTime = file.lastModified();

    if ((newSize != oldSize) || (newTime != oldTime)) {
      Log.d(LOG_TAG, "package size: " + oldSize + " -> " + newSize);
      Log.d(LOG_TAG, "package time: " + oldTime + " -> " + newTime);

      extractAssets();

      {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(prefKey_size, newSize);
        editor.putLong(prefKey_time, newTime);
        editor.commit();
      }
    }
  }

  public static void begin (Context context) {
    currentContext = context;

    dataDirectory = context.getDir(LIBRARY_NAME, Context.MODE_WORLD_READABLE);
    setDataPath(dataDirectory.getAbsolutePath());

    updatePackageData();
  }

  public static void end () {
    currentContext = null;
    dataDirectory = null;
  }

  private Louis () {
  }
}
