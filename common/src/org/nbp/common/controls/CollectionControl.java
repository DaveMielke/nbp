package org.nbp.common.controls;
import org.nbp.common.*;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import android.util.Log;
import android.content.SharedPreferences;

public abstract class CollectionControl<V> extends ItemControl {
  private final static String LOG_TAG = CollectionControl.class.getName();

  protected abstract V getCollectionDefault ();
  protected abstract V getCollectionValue ();
  protected abstract boolean setCollectionValue (V value);

  protected abstract String getValueName (V value);
  protected abstract String getValueLabel (V value);

  private final ArrayList<V> valueList = new ArrayList<V>();
  private final Map<String, Integer> valueIndex = new HashMap<String, Integer>();

  protected final void addCollectionValue (V value) {
    synchronized (this) {
      int index = valueList.size();
      valueList.add(value);
      valueIndex.put(getValueName(value), index);
    }
  }

  public final V getValue (int index) {
    return valueList.get(index);
  }

  public final V getValue (String key) {
    Integer index = valueIndex.get(key);
    if (index == null) return null;
    return getValue(index);
  }

  @Override
  protected final int getValueCount () {
    return valueList.size();
  }

  @Override
  protected final String getValueLabel (int index) {
    return getValueLabel(getValue(index));
  }

  protected boolean testCollectionValue (V value) {
    return true;
  }

  @Override
  protected final boolean testIntegerValue (int value) {
    return testCollectionValue(getValue(value));
  }

  @Override
  public final CharSequence[] getHighlightedLabels () {
    int count = valueList.size();
    CharSequence[] labels = new CharSequence[count];

    for (int index=0; index<count; index+=1) {
      V value = valueList.get(index);
      CharSequence label = getValueLabel(value);
      if (!testCollectionValue(value)) label = highlightUnselectableLabel(label);
      labels[index] = label;
    }

    return labels;
  }

  private final int getIntegerValue (V value) {
    Integer index = valueIndex.get(getValueName(value));
    if (index == null) return 0;
    return index;
  }

  @Override
  protected final int getIntegerDefault () {
    return getIntegerValue(getCollectionDefault());
  }

  @Override
  public final int getIntegerValue () {
    return getIntegerValue(getCollectionValue());
  }

  @Override
  protected final boolean setIntegerValue (int value) {
    if (value < 0) return false;
    if (value >= valueList.size()) return false;
    return setCollectionValue(valueList.get(value));
  }

  public final boolean setValue (V value) {
    synchronized (this) {
      if (value == getCollectionValue()) return true;
      if (!testCollectionValue(value)) return false;
      if (!setCollectionValue(value)) return false;
      reportValueChange();
    }

    return true;
  }

  @Override
  public CharSequence getValue () {
    return getValueLabel(getCollectionValue());
  }

  @Override
  protected void saveValue (SharedPreferences.Editor editor, String key) {
    editor.putString(key, getValueName(getCollectionValue()));
  }

  @Override
  protected ValueRestorer getValueRestorer () {
    return new ValueRestorer<V>() {
      @Override
      protected V getDefaultValue () {
        return getCollectionDefault();
      }

      @Override
      protected V getSavedValue (SharedPreferences prefs, String key, V defaultValue) {
        String name = prefs.getString(key, "");
        if (name.isEmpty()) return defaultValue;

        Integer index = valueIndex.get(name);
        if (index == null) return defaultValue;

        return valueList.get(index);
      }

      @Override
      protected boolean setCurrentValue (V value) {
        return setCollectionValue(value);
      }

      @Override
      protected boolean testValue (V value) {
        return testCollectionValue(value);
      }
    };
  }

  protected CollectionControl () {
    super();
  }
}
