package org.nbp.editor;

import java.io.File;

import org.nbp.common.CommonActivity;

import android.util.Log;

import android.content.Context;
import android.content.Intent;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;

import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import android.widget.EditText;
import android.text.InputFilter;

import android.text.Spanned;
import android.text.SpannableStringBuilder;

import android.view.Menu;
import android.view.MenuItem;

public class EditorActivity extends CommonActivity {
  private final static String LOG_TAG = EditorActivity.class.getName();

  private EditText editArea = null;
  private TextView currentPath = null;
  private File currentFile = null;
  private boolean hasChanged = false;

  protected final Activity getActivity () {
    return this;
  }

  private final void showActivityResultCode (int code) {
  }

  private void setCurrentFile (File file, CharSequence content) {
    String path;

    if (file != null) {
      path = file.getAbsolutePath();
    } else {
      path = getString(R.string.message_new_file);
    }

    synchronized (this) {
      currentFile = file;
      currentPath.setText(path);
      editArea.setText(content);
      hasChanged = false;
    }
  }

  private void setCurrentFile () {
    setCurrentFile(null, "");
  }

  private final void editFile (final File file) {
    new AsyncTask<Void, Void, CharSequence>() {
      @Override
      protected CharSequence doInBackground (Void... arguments) {
        final SpannableStringBuilder sb = new SpannableStringBuilder();
        FileHandler.get(file).read(file, sb);
        return sb.subSequence(0, sb.length());
      }

      @Override
      protected void onPostExecute (CharSequence content) {
        setCurrentFile(file, content);
      }
    }.execute();
  }

  private void newFile () {
    setCurrentFile();
  }

  private void openFile () {
    findFile(
      new ActivityResultHandler() {
        @Override
        public void handleActivityResult (int code, Intent intent) {
          switch (code) {
            case RESULT_OK:
              editFile(new File(intent.getData().getPath()));
              break;

            default:
              showActivityResultCode(code);
              break;
          }
        }
      }
    );
  }

  private void saveFile () {
    if (currentFile == null) saveFileAs();
  }

  private void saveFileAs () {
  }

  private void sendFile () {
  }

  @Override
  public boolean onOptionsItemSelected (MenuItem item) {
    switch (item.getItemId()) {
      case  R.id.options_new:
        newFile();
        return true;

      case  R.id.options_open:
        openFile();
        return true;

      case  R.id.options_save:
        saveFile();
        return true;

      case  R.id.options_saveAs:
        saveFileAs();
        return true;

      case  R.id.options_send:
        sendFile();
        return true;

      default:
        return false;
    }
  }

  @Override
  public boolean onCreateOptionsMenu (Menu menu) {
    getMenuInflater().inflate(R.menu.options, menu);
    return true;
  }

  private final void prepareActionsButton () {
    if (getActionBar() == null) {
      Button button = (Button)findViewById(R.id.actions_button);
      button.setVisibility(button.VISIBLE);

      button.setOnClickListener(
        new Button.OnClickListener() {
          @Override
          public void onClick (View view) {
            getActivity().openOptionsMenu();
          }
        }
      );
    }
  }

  private void setInputFilters () {
    editArea.setFilters(
      new InputFilter[] {
        new InputFilter() {
          @Override
          public CharSequence filter (
            CharSequence src, int srcStart, int srcEnd,
            Spanned dst, int dstStart, int dstEnd
          ) {
            while (srcStart < srcEnd) {
              char character = src.charAt(srcStart++);

              switch (character) {
                case 0X0E:
                  newFile();
                  break;

                case 0X0F:
                  openFile();
                  break;

                case 0X13:
                  saveFile();
                  break;

                default:
                  if (Character.getType(character) == Character.CONTROL) break;

                case '\f':
                case '\n':
                case '\r':
                case '\t':
                  hasChanged = true;
                  return null;
              }
            }

            return "";
          }
        }
      }
    );
  }

  @Override
  public void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.editor);
    currentPath = (TextView)findViewById(R.id.current_file);
    editArea = (EditText)findViewById(R.id.edit_area);
    setCurrentFile();

    prepareActionsButton();
    setInputFilters();
  }
}