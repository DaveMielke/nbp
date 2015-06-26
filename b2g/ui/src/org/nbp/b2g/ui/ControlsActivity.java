package org.nbp.b2g.ui;

import java.util.List;
import java.util.ArrayList;

import android.util.Log;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.GridLayout;

import android.widget.TextView;
import android.widget.Button;

import android.widget.CompoundButton;
import android.widget.Switch;

public class ControlsActivity extends InternalActivity {
  private final static String LOG_TAG = ControlsActivity.class.getName();

  private void updateWidget (Runnable runnable) {
    runOnUiThread(runnable);
  }

  private final List<View> developerViews = new ArrayList<View>();

  private void setDeveloperViewVisibility (boolean visible) {
    int visibility = visible? View.VISIBLE: View.GONE;

    for (View view : developerViews) {
      view.setVisibility(visibility);
    }
  }

  private void setDeveloperViewVisibility (Control control) {
    setDeveloperViewVisibility(((BooleanControl)control).getBooleanValue());
  }

  private void addDeveloperActionsControlListener () {
    Control control = Controls.getDeveloperActionsControl();
    setDeveloperViewVisibility(control);

    control.addOnValueChangedListener(new Control.OnValueChangedListener() {
      @Override
      public void onValueChanged (final Control control) {
        updateWidget(new Runnable() {
          @Override
          public void run () {
            setDeveloperViewVisibility(control);
          }
        });
      }
    });
  }

  private static void setChecked (CompoundButton button, Control control) {
    button.setChecked(((BooleanControl)control).getBooleanValue());
  }

  private Button createButton (String label, Button.OnClickListener listener) {
    Button button = new Button(this);
    button.setText(label);
    button.setOnClickListener(listener);
    return button;
  }

  private Button createButton (int label, Button.OnClickListener listener) {
    return createButton(ApplicationContext.getString(label), listener);
  }

  private View createSaveControlsButton () {
    Button button = createButton(
      R.string.saveControls_action_label,
      new Button.OnClickListener() {
        @Override
        public void onClick (View view) {
          Controls.saveValues();
          ApplicationUtilities.message(R.string.saveControls_action_confirmation);
        }
      }
    );

    return button;
  }

  private View createRestoreControlsButton () {
    Button button = createButton(
      R.string.restoreControls_action_label,
      new Button.OnClickListener() {
        @Override
        public void onClick (View view) {
          Controls.restoreSavedValues();
          ApplicationUtilities.message(R.string.restoreControls_action_confirmation);
        }
      }
    );

    return button;
  }

  private View createResetControlsButton () {
    Button button = createButton(
      R.string.resetControls_action_label,
      new Button.OnClickListener() {
        @Override
        public void onClick (View view) {
          Controls.restoreDefaultValues();
          ApplicationUtilities.message(R.string.resetControls_action_confirmation);
        }
      }
    );

    return button;
  }

  private View createActionsView () {
    LinearLayout view = new LinearLayout(this);
    view.setOrientation(view.HORIZONTAL);

    LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.WRAP_CONTENT,
      LinearLayout.LayoutParams.MATCH_PARENT
    );

    view.addView(createSaveControlsButton(), parameters);
    view.addView(createRestoreControlsButton(), parameters);
    view.addView(createResetControlsButton(), parameters);

    return view;
  }

  private View createNextValueButton (final Control control) {
    Button button = createButton(
      control.getNextLabel(),
      new Button.OnClickListener() {
        @Override
        public void onClick (View view) {
          if (!control.nextValue()) {
            ApplicationUtilities.beep();
          }
        }
      }
    );

    return button;
  }

  private View createPreviousValueButton (final Control control) {
    Button button = createButton(
      control.getPreviousLabel(),
      new Button.OnClickListener() {
        @Override
        public void onClick (View view) {
          if (!control.previousValue()) {
            ApplicationUtilities.beep();
          }
        }
      }
    );

    return button;
  }

  private View createControlLabelView (Control control) {
    TextView view = new TextView(this);
    view.setText(control.getLabel());
    return view;
  }

  private View createBooleanValueView (final Control control) {
    final Switch view = new Switch(this);
    setChecked(view, control);

    view.setTextOff(control.getPreviousLabel());
    view.setTextOn(control.getNextLabel());

    Switch.OnCheckedChangeListener switchListener = new Switch.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged (CompoundButton button, boolean isChecked) {
        if (isChecked) {
          control.nextValue();
        } else {
          control.previousValue();
        }
      }
    };

    Control.OnValueChangedListener controlListener = new Control.OnValueChangedListener() {
      @Override
      public void onValueChanged (final Control control) {
        updateWidget(new Runnable() {
          @Override
          public void run () {
            setChecked((Switch)view, control);
          }
        });
      }
    };

    view.setOnCheckedChangeListener(switchListener);
    control.addOnValueChangedListener(controlListener);
    return view;
  }

  private View createIntegerValueView (Control control) {
    final TextView view = new TextView(this);
    view.setText(control.getValue());

    Control.OnValueChangedListener controlListener = new Control.OnValueChangedListener() {
      @Override
      public void onValueChanged (final Control control) {
        updateWidget(new Runnable() {
          @Override
          public void run () {
            TextView t = (TextView)view;
            t.setText(control.getValue());
          }
        });
      }
    };

    control.addOnValueChangedListener(controlListener);
    return view;
  }

  private View createControlsView () {
    final GridLayout view = new GridLayout(this);
    view.setOrientation(view.VERTICAL);

    Controls.forEachControl(new ControlProcessor() {
      private boolean isForDevelopers;

      private void setColumn (int row, int column, View content) {
        view.addView(content, new GridLayout.LayoutParams(view.spec(row), view.spec(column)));
        if (isForDevelopers) developerViews.add(content);
      }

      private void setRow (int row, Control control) {
        isForDevelopers = control.isForDevelopers();
        setColumn(row, 0, createControlLabelView(control));

        if (control instanceof BooleanControl) {
          setColumn(row, 1, createBooleanValueView(control));
        } else {
          setColumn(row, 1, createIntegerValueView(control));
          setColumn(row, 2, createPreviousValueButton(control));
          setColumn(row, 3, createNextValueButton(control));
        }
      }

      @Override
      public boolean processControl (Control control) {
        int row = view.getRowCount();
        setRow(row, control);
        return true;
      }
    });

    addDeveloperActionsControlListener();
    return createVerticalScrollView(view);
  }

  private View createRootView () {
    LinearLayout view = new LinearLayout(this);
    view.setOrientation(view.VERTICAL);

     LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
    );

    parameters.leftMargin = ApplicationContext.dipsToPixels(
      ApplicationParameters.SCREEN_LEFT_OFFSET
    );

    view.addView(createActionsView(), parameters);
    view.addView(createControlsView(), parameters);

    view.setLayoutParams(new ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT
    ));

    return view;
  }

  @Override
  public void onCreate (Bundle state) {
    super.onCreate(state);
    ApplicationContext.setContext(this);
    setContentView(createRootView());
  }
}
