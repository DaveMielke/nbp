package org.nbp.b2g.ui;

import java.io.IOException;

import android.util.Log;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;

import android.os.PowerManager;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import android.os.RecoverySystem;

public class MaintenanceActivity extends ProgrammaticActivity {
  private final static String LOG_TAG = MaintenanceActivity.class.getName();

  private Context getContext () {
    return MaintenanceActivity.this;
  }

  private PowerManager getPowerManager () {
    return (PowerManager)getSystemService(Context.POWER_SERVICE);
  }

  private TextView messageView;

  private void setMessage (String message) {
    Devices.braille.get().write(message);
    Devices.speech.get().say(message);
    messageView.setText(message);
  }

  private void setMessage (int message) {
    setMessage(getString(message));
  }

  private enum ActivityRequestType {
    FIND_OTA_UPDATE;
  }

  @Override
  protected void onActivityResult (int requestCode, int resultCode, Intent resultData) {
    ActivityRequestType requestType = ActivityRequestType.values()[requestCode];
  }

  private void findFile (ActivityRequestType requestType) {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("file/*");

    try {
      startActivityForResult(intent, requestType.ordinal());
    } catch (ActivityNotFoundException exception) {
      Log.w(LOG_TAG, "file system browser not found: " + exception.getMessage());
    }
  }

  private String getRebootFailureMessage () {
    return getString(R.string.maintenance_message_reboot_failed);
  }

  private void rebootDevice (String reason) {
    getPowerManager().reboot(reason);
    setMessage(getRebootFailureMessage());
  }

  private View createRestartSystemButton () {
    Button button = createButton(
      R.string.maintenance_RestartSystem_label,
      new Button.OnClickListener() {
        @Override
        public void onClick (View view) {
          setMessage(R.string.maintenance_RestartSystem_starting);
          rebootDevice(null);
        }
      }
    );

    return button;
  }

  private View createRecoveryModeButton () {
    Button button = createButton(
      R.string.maintenance_RecoveryMode_label,
      new Button.OnClickListener() {
        @Override
        public void onClick (View view) {
          setMessage(R.string.maintenance_RecoveryMode_starting);
          rebootDevice("recovery");
        }
      }
    );

    return button;
  }

  private View createBootLoaderButton () {
    Button button = createButton(
      R.string.maintenance_BootLoader_label,
      new Button.OnClickListener() {
        @Override
        public void onClick (View view) {
          setMessage(R.string.maintenance_BootLoader_starting);
          rebootDevice("bootloader");
        }
      }
    );

    return button;
  }

  private View createUpdateSystemButton () {
    Button button = createButton(
      R.string.maintenance_UpdateSystem_label,
      new Button.OnClickListener() {
        @Override
        public void onClick (View view) {
          setMessage(R.string.maintenance_UpdateSystem_finding);
          findFile(ActivityRequestType.FIND_OTA_UPDATE);
        }
      }
    );

    return button;
  }

  private View createClearCacheButton () {
    Button button = createButton(
      R.string.maintenance_ClearCache_label,
      new Button.OnClickListener() {
        @Override
        public void onClick (View view) {
          setMessage(R.string.maintenance_ClearCache_starting);
          String failure = getRebootFailureMessage();

          try {
            RecoverySystem.rebootWipeCache(getContext());
          } catch (IOException exception) {
            failure = exception.getMessage();
          }

          setMessage(failure);
        }
      }
    );

    return button;
  }

  private View createFactoryResetButton () {
    Button button = createButton(
      R.string.maintenance_FactoryReset_label,
      new Button.OnClickListener() {
        @Override
        public void onClick (View view) {
          setMessage(R.string.maintenance_FactoryReset_starting);
          String failure = getRebootFailureMessage();

          try {
            RecoverySystem.rebootWipeUserData(getContext());
          } catch (IOException exception) {
            failure = exception.getMessage();
          }

          setMessage(failure);
        }
      }
    );

    return button;
  }

  @Override
  protected final View createContentView () {
    LinearLayout view = new LinearLayout(this);
    view.setOrientation(view.VERTICAL);

    LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
    );

    parameters.leftMargin = ApplicationContext.dipsToPixels(
      ApplicationParameters.SCREEN_LEFT_OFFSET
    );

    messageView = createTextView();
    view.addView(messageView);

    view.addView(createRestartSystemButton());
    view.addView(createRecoveryModeButton());
    view.addView(createBootLoaderButton());

    view.addView(createUpdateSystemButton());
    view.addView(createClearCacheButton());
    view.addView(createFactoryResetButton());

    return view;
  }

  @Override
  public void onCreate (Bundle state) {
    super.onCreate(state);
    ApplicationContext.setContext(this);
    setContentView();
  }
}
