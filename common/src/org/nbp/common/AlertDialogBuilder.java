package org.nbp.common;

import java.util.Map;
import java.util.HashMap;

import android.util.Log;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ListView;

import android.content.Context;
import android.view.KeyEvent;
import android.view.ContextThemeWrapper;

public class AlertDialogBuilder extends AlertDialog.Builder {
  private final static String LOG_TAG = AlertDialogBuilder.class.getName();

  private interface ListViewOperation {
    public void perform (ListView listView);
  }

  private final static Map<Integer, ListViewOperation> listViewOperations =
               new HashMap<Integer, ListViewOperation>()
  {
    {
      put(KeyEvent.KEYCODE_DPAD_DOWN,
        new ListViewOperation() {
          @Override
          public void perform (ListView listView) {
          }
        }
      );
    }
  };

  private final void setKeyListener () {
    setOnKeyListener(
      new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey (DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
          Log.d(LOG_TAG, ("key code: " + keyCode));

          AlertDialog alertDialog = (AlertDialog)dialogInterface;
          ListView listView = alertDialog.getListView();

          if (listView != null) {
            ListViewOperation operation = listViewOperations.get(keyCode);

            if (operation != null) {
              if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                operation.perform(listView);
              }

              return true;
            }
          }

          return false;
        }
      }
    );
  }

  private final void setTitle (int subtitle, String detail) {
    Context context = getContext();

    StringBuilder title = new StringBuilder();
    String delimiter = " - ";

    {
      String name = CommonContext.getApplicationName();

      if ((name != null) && !name.isEmpty()) {
        title.append(name);
        title.append(delimiter);
      }
    }

    title.append(context.getString(subtitle));

    if ((detail != null) && !detail.isEmpty()) {
      title.append(delimiter);
      title.append(detail);
    }

    setTitle(title.toString());
  }

  public AlertDialogBuilder (Context context, int subtitle, String detail) {
    super(context);
    setTitle(subtitle, detail);
    setKeyListener();
  }
}
