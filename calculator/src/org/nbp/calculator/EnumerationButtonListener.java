package org.nbp.calculator;

import java.util.Map;
import java.util.HashMap;

import org.nbp.common.CommonUtilities;
import org.nbp.common.LanguageUtilities;

import android.app.AlertDialog;
import android.content.DialogInterface;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class EnumerationButtonListener<E extends Enum<E>> {
  public interface ChangeHandler {
    public void handleChange (Enum value);
  }

  private final Class<E> enumerationType;
  private final E[] enumerationValues;
  private final Map<String, Enum> enumerationNames = new HashMap<String, Enum>();

  private final Activity mainActivity;
  private final Button changeButton;
  private final ChangeHandler changeHandler;
  private final String settingName;
  private final E initialValue;

  private final <T> T getProperty (int index, String methodName) {
    return (T)LanguageUtilities.invokeInstanceMethod(
      enumerationValues[index], methodName
    );
  }

  private final <T> T getProperty (String methodName) {
    return getProperty(0, methodName);
  }

  private final int getTitle () {
    return getProperty("getTitle");
  }

  private final String getLabel (int index) {
    return getProperty(index, "getLabel");
  }

  private final String getDescription (int index) {
    int description = getProperty(index, "getDescription");
    return mainActivity.getString(description);
  }

  private final void changeSetting (int index) {
    Enum value = enumerationValues[index];
    changeButton.setTag(value);
    changeButton.setText(getLabel(index));
    changeButton.setContentDescription(getDescription(index));

    if (settingName != null) {
      SavedSettings.set(settingName, value);
    }

    if (changeHandler != null) {
      changeHandler.handleChange(value);
    }
  }

  private final void changeSetting (Enum value) {
    changeSetting(value.ordinal());
  }

  private final void changeSetting (String name) {
    changeSetting(enumerationNames.get(name));
  }

  private final void registerCycleListener () {
    if (CommonUtilities.haveKitkat) {
      changeButton.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
    }

    changeButton.setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
          E value = (E)changeButton.getTag();
          int index = value.ordinal();
          if ((index += 1) == enumerationValues.length) index = 0;
          changeSetting(index);
        }
      }
    );
  }

  private final void registerSelectListener () {
    changeButton.setOnLongClickListener(
      new View.OnLongClickListener() {
        @Override
        public boolean onLongClick (View view) {
          AlertDialog.Builder builder = ApplicationUtilities.newAlertDialogBuilder(mainActivity, getTitle());

          {
            int count = enumerationValues.length;
            CharSequence[] items = new CharSequence[count];

            for (int index=0; index<count; index+=1) {
              items[index] = getDescription(index);
            }

            builder.setItems(items,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick (DialogInterface dialog, int index) {
                changeSetting(index);
              }
            }
            );
          }

          builder.setNegativeButton(
            R.string.button_cancel,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick (DialogInterface dialog, int button) {
              }
            }
          );

          builder.show();
          return true;
        }
      }
    );
  }

  public EnumerationButtonListener (
    Activity activity, Button button, Class<E> type,
    String setting, E initial, ChangeHandler handler
  ) {
    mainActivity = activity;
    changeButton = button;
    changeHandler = handler;
    enumerationType = type;
    settingName = setting;

    enumerationValues = (E[])LanguageUtilities.invokeStaticMethod(
      enumerationType, "values"
    );

    for (Enum value : enumerationValues) {
      enumerationNames.put(value.name(), value);
    }

    if (initial == null) initial = enumerationValues[0];
    initialValue = initial;

    if (settingName != null) {
      changeSetting(SavedSettings.get(settingName, type, initialValue));
    }

    registerCycleListener();
    registerSelectListener();
  }

  public EnumerationButtonListener (
    Activity activity, Button button, Class<E> type,
    String setting, E initial
  ) {
    this(activity, button, type, setting, initial, null);
  }

  public EnumerationButtonListener (
    Activity activity, Button button, Class<E> type, String setting
  ) {
    this(activity, button, type, setting, null);
  }

  public EnumerationButtonListener (Activity activity, Button button, Class<E> type) {
    this(activity, button, type, null);
  }
}
