package org.nbp.calculator;

import java.util.Collections;
import java.util.Collection;

import java.util.List;
import java.util.ArrayList;

import java.util.Set;
import java.util.HashSet;

import org.nbp.common.CommonActivity;
import org.nbp.common.AlertDialogBuilder;
import org.nbp.common.CharacterUtilities;
import org.nbp.common.OnTextEditedListener;

import android.util.Log;
import android.util.TypedValue;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CompoundButton;

import android.text.InputFilter;
import android.text.Spanned;

import android.app.AlertDialog;
import android.content.DialogInterface;

import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.SuperscriptSpan;
import android.text.style.RelativeSizeSpan;

public class CalculatorActivity extends CommonActivity {
  private final static String LOG_TAG = CalculatorActivity.class.getName();

  private static String[] toArray (Collection<String> collection) {
    return collection.toArray(new String[collection.size()]);
  }

  private final View findView (DialogInterface dialog, int id) {
    return ((AlertDialog)dialog).findViewById(id);
  }

  private final AlertDialog.Builder newAlertDialogBuilder (int... subtitles) {
    return new AlertDialogBuilder(this, subtitles)
              .setNegativeButton(R.string.button_cancel, null)
              .setCancelable(true)
              ;
  }

  private final void setClickListener (int id, View.OnClickListener listener) {
    View view = findViewById(id);
    view.setOnClickListener(listener);
  }

  private final void setLongClickListener (int id, View.OnLongClickListener listener) {
    View view = findViewById(id);
    view.setOnLongClickListener(listener);
  }

  private final void setToggleButtonListener (
    int id, final String settingName, final boolean defaultValue,
    final TextView text, final int off, final int on
  ) {
    Button button = (Button)findViewById(id);
    boolean value = SavedSettings.get(settingName, defaultValue);
    text.setText(value? on: off);

    setClickListener(
      id,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
          boolean value = !SavedSettings.get(settingName, defaultValue);
          text.setText(value? on: off);
          SavedSettings.set(settingName, value);
        }
      }
    );
  }

  private EditText expressionView;
  private TextView resultView;
  private TextView angleUnitView;

  private Button leftButton;
  private Button rightButton;
  private Button backspaceButton;
  private Button deleteButton;
  private Button upButton;
  private Button downButton;

  private final void setNavigationButtonStates () {
    int length = expressionView.length();
    int start = expressionView.getSelectionStart();
    int end = expressionView.getSelectionEnd();

    {
      boolean enabled = end > 0;
      leftButton.setEnabled(enabled);
      backspaceButton.setEnabled(enabled);
    }

    {
      boolean enabled = start < length;
      rightButton.setEnabled(enabled);
      deleteButton.setEnabled(enabled);
    }

    upButton.setEnabled(false);
    downButton.setEnabled(false);
  }

  private final void saveExpression () {
    SavedSettings.set(SavedSettings.EXPRESSION, expressionView.getText().toString());
    SavedSettings.set(SavedSettings.START, expressionView.getSelectionStart());
    SavedSettings.set(SavedSettings.END, expressionView.getSelectionEnd());
  }

  private final void restoreExpression () {
    String expression = SavedSettings.get(SavedSettings.EXPRESSION, "");
    expressionView.setText(expression);

    int start = SavedSettings.get(SavedSettings.START, -1);
    int end = SavedSettings.get(SavedSettings.END, -1);
    int length = expression.length();

    if ((0 <= start) && (start <= end) && (end <= length)) {
      expressionView.setSelection(start, end);
    } else {
      expressionView.setSelection(length);
    }
  }

  private final void evaluateExpression (boolean showError) {
    saveExpression();
    String expression = expressionView.getText().toString();

    try {
      ExpressionEvaluation evaluation = new ExpressionEvaluation(expression);
      ComplexNumber result = evaluation.getResult();

      resultView.setText(result.format());
      SavedSettings.set(SavedSettings.RESULT, result);
    } catch (ExpressionException exception) {
      if (showError) {
        resultView.setText(exception.getMessage());
        expressionView.setSelection(exception.getLocation());
      }
    }
  }

  private final void setEvaluateListener () {
    expressionView.setOnEditorActionListener(
      new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction (TextView view, int action, KeyEvent event) {
          if (event != null) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
              if (event.getAction() == KeyEvent.ACTION_DOWN) {
                evaluateExpression(true);
                resultView.requestFocus();
              }

              return true;
            }
          }

          return false;
        }
      }
    );
  }

  private final void insertExpressionText (String text) {
    int start = expressionView.getSelectionStart();
    int end = expressionView.getSelectionEnd();
    expressionView.getText().replace(start, end, text);

    int left = text.indexOf(Function.ARGUMENT_PREFIX);
    int right = text.lastIndexOf(Function.ARGUMENT_SUFFIX);
    int length = text.length();

    if ((0 <= left) && (left < right) && (right < length)) {
      end = start + right;
      start += left + 1;
    } else {
      end = start += length;
    }

    expressionView.setSelection(start, end);
  }

  private final void performClick (int view) {
    findViewById(view).performClick();
  }

  private final void setExpressionListener () {
    expressionView.setFilters(
      new InputFilter[] {
        new InputFilter() {
          private final boolean handleCharacter (char character) {
            switch (character) {
              case CharacterUtilities.CHAR_ETX: // control C
                performClick(R.id.button_clear);
                return true;

              case CharacterUtilities.CHAR_ENQ: // control E
                performClick(R.id.button_erase);
                return true;

              case CharacterUtilities.CHAR_ACK: // control F
                performClick(R.id.button_functions);
                return true;

              case CharacterUtilities.CHAR_DC3: // control S
                performClick(R.id.button_store);
                return true;

              case CharacterUtilities.CHAR_SYN: // control V
                performClick(R.id.button_variables);
                return true;

              default:
                return Character.isISOControl(character);
            }
          }

          @Override
          public CharSequence filter (
            CharSequence src, int srcStart, int srcEnd,
            Spanned dst, int dstStart, int dstEnd
          ) {
            if ((srcStart + 1) == srcEnd) {
              if (handleCharacter(src.charAt(srcStart))) {
                return "";
              }
            }

            return null;
          }
        }
      }
    );

    new OnTextEditedListener(expressionView) {
      @Override
      public void onTextEdited (boolean isDifferent) {
        if (isDifferent) {
          evaluateExpression(false);
          setNavigationButtonStates();
        }
      }
    };
  }

  private ViewGroup[] keypadViews;
  private int currentKeypad;

  private final void showKeypad () {
    View keypad = keypadViews[currentKeypad];

    for (View view : keypadViews) {
      view.setVisibility((view == keypad)? View.VISIBLE: View.GONE);
    }
  }

  private final void showKeypad (int index) {
    if (index != currentKeypad) {
      currentKeypad = index;
      showKeypad();
    }
  }

  private final void setFocusToKeypad () {
    ViewGroup keypad = keypadViews[currentKeypad];
    keypad.getChildAt(0).requestFocus();
  }

  private final static CharacterStyle exponentSpans[] = {
    new SuperscriptSpan(),
    new RelativeSizeSpan(0.6f)
  };

  private final void prepareKeypads (Integer... ids) {
    View.OnClickListener insertListener = new View.OnClickListener() {
      @Override
      public void onClick (View view) {
        Button button = (Button)view;
        insertExpressionText(button.getHint().toString());
        expressionView.requestFocus();
        showKeypad(0);
      }
    };

    View.OnClickListener evaluateListener = new View.OnClickListener() {
      @Override
      public void onClick (View view) {
        evaluateExpression(true);
        resultView.requestFocus();
        showKeypad(0);
      }
    };

    keypadViews = new ViewGroup[ids.length];
    int keypadCount = 0;

    for (int id : ids) {
      ViewGroup keypad = (ViewGroup)findViewById(id);
      keypadViews[keypadCount++] = keypad;
      int rowCount = keypad.getChildCount();

      for (int rowIndex=0; rowIndex<rowCount; rowIndex+=1) {
        ViewGroup row = (ViewGroup)keypad.getChildAt(rowIndex);
        int keyCount = row.getChildCount();

        for (int keyIndex=0; keyIndex<keyCount; keyIndex+=1) {
          TextView key = (TextView)row.getChildAt(keyIndex);
          key.setBackgroundColor(0);

          if (id == R.id.keypad_numeric) {
            key.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
          }

          String text = key.getText().toString();
          CharSequence hint = key.getHint();

          if (text.equals("=")) {
            key.setOnClickListener(evaluateListener);
          } else {
            key.setOnClickListener(insertListener);

            if ((hint == null) || (hint.length() == 0)) {
              Function function = Functions.get(text);

              if (function != null) {
                hint = function.getCall();
              } else {
                hint = text;
              }

              key.setHint(hint);
            }

            {
              int index = text.indexOf('^');

              if (index >= 0) {
                SpannableStringBuilder sb = new SpannableStringBuilder();
                sb.append(text.substring(0, index));
                sb.append(text.substring(index+1));
                int length = sb.length();

                for (CharacterStyle span : exponentSpans) {
                  sb.setSpan(span, index, length, sb.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                key.setText(sb.subSequence(0, length));
              }
            }
          }
        }
      }
    }
  }

  private final void setClearButtonListener () {
    setClickListener(
      R.id.button_clear,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
          expressionView.setText("");
          expressionView.requestFocus();
        }
      }
    );

    setLongClickListener(
      R.id.button_clear,
      new View.OnLongClickListener() {
        @Override
        public boolean onLongClick (View view) {
          resultView.setText("");
          SavedSettings.set(SavedSettings.RESULT, Double.NaN);

          expressionView.setText("");
          expressionView.requestFocus();
          return true;
        }
      }
    );
  }

  private final void setShiftButtonListener () {
    setClickListener(
      R.id.button_shift,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
          currentKeypad += 1;
          currentKeypad %= keypadViews.length;

          showKeypad();
          setFocusToKeypad();
        }
      }
    );

    setLongClickListener(
      R.id.button_shift,
      new View.OnLongClickListener() {
        @Override
        public boolean onLongClick (View view) {
          showKeypad(0);
          setFocusToKeypad();
          return true;
        }
      }
    );
  }

  private final void setAngleUnitButtonListener () {
    setToggleButtonListener(
      R.id.button_angleUnit,
      SavedSettings.DEGREES,
      DefaultSettings.DEGREES,
      angleUnitView, R.string.angleUnit_radians, R.string.angleUnit_degrees
    );
  }

  private final void setLeftButtonListener () {
    setClickListener(
      R.id.button_left,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
          int start = expressionView.getSelectionStart();

          if (start > 0) {
            expressionView.setSelection(start-1);
            setNavigationButtonStates();
          }
        }
      }
    );

    setLongClickListener(
      R.id.button_left,
      new View.OnLongClickListener() {
        @Override
        public boolean onLongClick (View view) {
          if (expressionView.getSelectionStart() > 0) {
            expressionView.setSelection(0);
            setNavigationButtonStates();
          }

          return true;
        }
      }
    );
  }

  private final void setRightButtonListener () {
    setClickListener(
      R.id.button_right,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
          int end = expressionView.getSelectionEnd();

          if (end < expressionView.length()) {
            expressionView.setSelection(end+1);
            setNavigationButtonStates();
          }
        }
      }
    );

    setLongClickListener(
      R.id.button_left,
      new View.OnLongClickListener() {
        @Override
        public boolean onLongClick (View view) {
          int length = expressionView.length();

          if (expressionView.getSelectionEnd() < length) {
            expressionView.setSelection(length);
            setNavigationButtonStates();
          }

          return true;
        }
      }
    );
  }

  private final void setBackspaceButtonListener () {
    setClickListener(
      R.id.button_backspace,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
          int start = expressionView.getSelectionStart();
          int end = expressionView.getSelectionEnd();

          if (start == end) {
            if (start == 0) return;
            end = start;
            start -= 1;
          }

          expressionView.getText().delete(start, end);
          setNavigationButtonStates();
        }
      }
    );

    setLongClickListener(
      R.id.button_left,
      new View.OnLongClickListener() {
        @Override
        public boolean onLongClick (View view) {
          int end = expressionView.getSelectionEnd();

          if (end > 0) {
            expressionView.getText().delete(0, end);
            setNavigationButtonStates();
          }

          return true;
        }
      }
    );
  }

  private final void setDeleteButtonListener () {
    setClickListener(
      R.id.button_delete,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
          int start = expressionView.getSelectionStart();
          int end = expressionView.getSelectionEnd();

          if (start == end) {
            if (end == expressionView.length()) return;
            end = start + 1;
          }

          expressionView.getText().delete(start, end);
          setNavigationButtonStates();
        }
      }
    );

    setLongClickListener(
      R.id.button_left,
      new View.OnLongClickListener() {
        @Override
        public boolean onLongClick (View view) {
          int start = expressionView.getSelectionStart();
          int length = expressionView.length();

          if (start < length) {
            expressionView.getText().delete(start, length);
            setNavigationButtonStates();
          }

          return true;
        }
      }
    );
  }

  private final void setUpButtonListener () {
    setClickListener(
      R.id.button_up,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
        }
      }
    );

    setLongClickListener(
      R.id.button_left,
      new View.OnLongClickListener() {
        @Override
        public boolean onLongClick (View view) {
          return true;
        }
      }
    );
  }

  private final void setDownButtonListener () {
    setClickListener(
      R.id.button_down,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
        }
      }
    );

    setLongClickListener(
      R.id.button_left,
      new View.OnLongClickListener() {
        @Override
        public boolean onLongClick (View view) {
          return true;
        }
      }
    );
  }

  private final String formatVariableLine (String name, ComplexNumber value, String description) {
    StringBuilder sb = new StringBuilder();

    sb.append(name);
    sb.append(" = ");
    sb.append(value.format());

    if (description != null) {
      sb.append(" (");
      sb.append(description);
      sb.append(')');
    }

    return sb.toString();
  }

  private final String formatVariableLine (String name, ComplexNumber value) {
    return formatVariableLine(name, value, null);
  }

  private final String getVariableName (List<String> variables, int index) {
    String variable = variables.get(index);
    return variable.substring(0, variable.indexOf(' '));
  }

  private final List<String> getUserVariableLines () {
    List<String> variables = new ArrayList<String>();

    for (String name : Variables.getUserVariableNames()) {
      variables.add(formatVariableLine(name, Variables.get(name)));
    }

    Collections.sort(variables);
    return variables;
  }

  private final void setVariablesButtonListener () {
    setClickListener(
      R.id.button_variables,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
          AlertDialog.Builder builder = newAlertDialogBuilder(R.string.button_variables);
          final List<String> variables = getUserVariableLines();

          for (String name : Variables.getSystemVariableNames()) {
            SystemVariable variable = Variables.getSystemVariable(name);
            variables.add(formatVariableLine(name, variable.getValue(), variable.getDescription()));
          }

          builder.setItems(
            toArray(variables),
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick (DialogInterface dialog, int index) {
                insertExpressionText(getVariableName(variables, index));
                expressionView.requestFocus();
              }
            }
          );

          builder.show();
        }
      }
    );
  }

  private final void setStoreButtonListener () {
    setClickListener(
      R.id.button_store,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
          AlertDialog.Builder builder = newAlertDialogBuilder(R.string.button_store);
          final ComplexNumber result = SavedSettings.getResult();

          if (result.isNaN()) {
            builder.setMessage(R.string.error_no_result);
          } else {
            final List<String> variables = getUserVariableLines();

            if (variables.isEmpty()) {
              builder.setMessage(R.string.error_no_variables);
            } else {
              builder.setItems(
                toArray(variables),
                new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick (DialogInterface dialog, int index) {
                    Variables.set(getVariableName(variables, index), result);
                  }
                }
              );
            }

            builder.setPositiveButton(
              R.string.button_new,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int button) {
                  AlertDialog.Builder builder = newAlertDialogBuilder(
                    R.string.button_store,
                    R.string.button_new
                  );

                  builder.setView(
                    getLayoutInflater().inflate(R.layout.variable_name, null)
                  );

                  builder.setPositiveButton(
                    R.string.button_store,
                    new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick (DialogInterface dialog, int button) {
                        EditText view = (EditText)findView(dialog, R.id.variable);
                        String name = view.getText().toString();
                        Variables.set(name, result);
                      }
                    }
                  );

                  AlertDialog alert = builder.create();
                  alert.show();

                  EditText variableName = (EditText)findView(alert, R.id.variable);
                  final Button storeButton = alert.getButton(alert.BUTTON_POSITIVE);
                  storeButton.setEnabled(false);

                  variableName.setFilters(
                    new InputFilter[] {
                      new InputFilter() {
                        @Override
                        public CharSequence filter (
                          CharSequence src, int srcStart, int srcEnd,
                          Spanned dst, int dstStart, int dstEnd
                        ) {
                          int dstIndex = dstStart;

                          for (int srcIndex=srcStart; srcIndex<srcEnd; srcIndex+=1) {
                            if (!Variables.isNameCharacter(src.charAt(srcIndex), (dstIndex == 0))) return "";
                            dstIndex += 1;
                          }

                          boolean isNew = false;
                          StringBuilder name = new StringBuilder(dst.toString());

                          name.delete(dstStart, dstEnd);
                          name.insert(dstStart, src.subSequence(srcStart, srcEnd));

                          if (name.length() > 0) {
                            if (Variables.isNameCharacter(name.charAt(0), true)) {
                              if (Variables.get(name.toString()) == null) {
                                isNew = true;
                              }
                            }
                          }

                          storeButton.setEnabled(isNew);
                          return null;
                        }
                      }
                    }
                  );
                }
              }
            );
          }

          builder.show();
        }
      }
    );
  }

  private final void setEraseButtonListener () {
    setClickListener(
      R.id.button_erase,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
          AlertDialog.Builder builder = newAlertDialogBuilder(R.string.button_erase);
          final List<String> variables = getUserVariableLines();

          if (variables.isEmpty()) {
            builder.setMessage(R.string.error_no_variables);
          } else {
            builder.setItems(
              toArray(variables),
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int index) {
                  Variables.removeUserVariable(getVariableName(variables, index));
                }
              }
            );
          }

          builder.show();
        }
      }
    );

    setLongClickListener(
      R.id.button_erase,
      new View.OnLongClickListener() {
        @Override
        public boolean onLongClick (View view) {
          AlertDialog.Builder builder = newAlertDialogBuilder(R.string.button_erase);
          final List<String> variables = getUserVariableLines();

          if (variables.isEmpty()) {
            builder.setMessage(R.string.error_no_variables);
          } else {
            final Set<String> names = new HashSet<String>();

            builder.setMultiChoiceItems(
              toArray(variables),
              null,
              new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int index, boolean isChecked) {
                  String name = getVariableName(variables, index);

                  if (isChecked) {
                    names.add(name);
                  } else {
                    names.remove(name);
                  }
                }
              }
            );

            builder.setPositiveButton(
              R.string.button_erase,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int button) {
                  for (String name : names) {
                    Variables.removeUserVariable(name);
                  }
                }
              }
            );
          }

          builder.show();
          return true;
        }
      }
    );
  }

  private final String formatFunctionLine (String name, Function function) {
    String line = function.getCall();

    String summary = function.getSummary();
    if (summary != null) line += ": " + summary;

    return line;
  }

  private final String getFunctionCall (List<String> functions, int index) {
    String function = functions.get(index);
    return function.substring(0, (function.indexOf(Function.ARGUMENT_SUFFIX) + 1));
  }

  private final List<String> getFunctionLines () {
    List<String> functions = new ArrayList<String>();

    for (String name : Functions.getNames()) {
      functions.add(formatFunctionLine(name, Functions.get(name)));
    }

    Collections.sort(functions);
    return functions;
  }

  private final void setFunctionsButtonListener () {
    setClickListener(
      R.id.button_functions,
      new View.OnClickListener() {
        @Override
        public void onClick (View view) {
          AlertDialog.Builder builder = newAlertDialogBuilder(R.string.button_functions);
          final List<String> functions = getFunctionLines();

          if (functions.isEmpty()) {
            builder.setMessage(R.string.error_no_functions);
          } else {
            builder.setItems(
              toArray(functions),
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int index) {
                  insertExpressionText(getFunctionCall(functions, index));
                  expressionView.requestFocus();
                }
              }
            );
          }

          builder.show();
        }
      }
    );
  }

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.calculator);

    expressionView = (EditText)findViewById(R.id.expression);
    resultView = (TextView)findViewById(R.id.result);
    angleUnitView = (TextView)findViewById(R.id.angleUnit);

    leftButton = (Button)findViewById(R.id.button_left);
    rightButton = (Button)findViewById(R.id.button_right);
    backspaceButton = (Button)findViewById(R.id.button_backspace);
    deleteButton = (Button)findViewById(R.id.button_delete);
    upButton = (Button)findViewById(R.id.button_up);
    downButton = (Button)findViewById(R.id.button_down);

    setEvaluateListener();
    setExpressionListener();
    restoreExpression();
    expressionView.requestFocus();

    setLeftButtonListener();
    setRightButtonListener();
    setBackspaceButtonListener();
    setDeleteButtonListener();
    setUpButtonListener();
    setDownButtonListener();
    setNavigationButtonStates();

    setClearButtonListener();
    setAngleUnitButtonListener();
    setShiftButtonListener();

    setVariablesButtonListener();
    setFunctionsButtonListener();
    setStoreButtonListener();
    setEraseButtonListener();

    prepareKeypads(
      R.id.keypad_numeric,
      R.id.keypad_function
    );

    currentKeypad = 0;
    showKeypad();
  }

  @Override
  protected void onPause () {
    try {
      saveExpression();
    } finally {
      super.onPause();
    }
  }
}
