package org.nbp.calculator;

import android.util.Log;

import org.nbp.common.CommonContext;
import android.content.Context;
import android.content.SharedPreferences;

public abstract class SavedSettings {
  private final static String LOG_TAG = SavedSettings.class.getName();

  public final static String CALCULATOR_MODE = "calculator-mode";
  public final static String DECIMAL_NOTATION = "decimal-notation";
  public final static String ANGLE_UNIT = "angle-unit";

  public final static String EXPRESSION = "expression";
  public final static String START = "start";
  public final static String END = "end";

  private static Context getContext () {
    return CommonContext.getContext();
  }

  private static SharedPreferences getSettings () {
    return getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
  }

  public final static boolean remove (String name) {
    SharedPreferences settings = getSettings();
    if (!settings.contains(name)) return false;

    settings.edit().remove(name).apply();
    return true;
  }

  public final static void set (String name, String value) {
    getSettings().edit().putString(name, value).apply();
  }

  public final static String get (String name, String defaultValue) {
    return getSettings().getString(name, defaultValue);
  }

  public final static void set (String name, Enum value) {
    getSettings().edit().putString(name, value.name()).apply();
  }

  public final static <E extends Enum<E>> E get (String name, Class<E> type, E defaultValue) {
    String setting = getSettings().getString(name, null);

    if (setting != null) {
      try {
        E value = Enum.valueOf(type, setting);
        if (value != null) return value;
      } catch (IllegalArgumentException exception) {
        Log.w(LOG_TAG, exception.getMessage());
      }
    }

    return defaultValue;
  }

  public final static void set (String name, boolean value) {
    getSettings().edit().putBoolean(name, value).apply();
  }

  public final static boolean get (String name, boolean defaultValue) {
    return getSettings().getBoolean(name, defaultValue);
  }

  public final static void set (String name, int value) {
    getSettings().edit().putInt(name, value).apply();
  }

  public final static int get (String name, int defaultValue) {
    return getSettings().getInt(name, defaultValue);
  }

  public final static void set (String name, long value) {
    getSettings().edit().putLong(name, value).apply();
  }

  public final static long get (String name, long defaultValue) {
    return getSettings().getLong(name, defaultValue);
  }

  public final static void set (String name, float value) {
    getSettings().edit().putFloat(name, value).apply();
  }

  public final static float get (String name, float defaultValue) {
    return getSettings().getFloat(name, defaultValue);
  }

  public final static void set (String name, double value) {
    getSettings().edit().putString(name, Double.toString(value)).apply();
  }

  public final static double get (String name, double defaultValue) {
    String string = getSettings().getString(name, null);
    return (string != null)? Double.valueOf(string): defaultValue;
  }

  public final static CalculatorMode getCalculatorMode () {
    return get(CALCULATOR_MODE, CalculatorMode.class, DefaultSettings.CALCULATOR_MODE);
  }

  public final static DecimalNotation getDecimalNotation () {
    return get(DECIMAL_NOTATION, DecimalNotation.class, DefaultSettings.DECIMAL_NOTATION);
  }

  public final static AngleUnit getAngleUnit () {
    return get(ANGLE_UNIT, AngleUnit.class, DefaultSettings.ANGLE_UNIT);
  }

  private SavedSettings () {
  }
}
