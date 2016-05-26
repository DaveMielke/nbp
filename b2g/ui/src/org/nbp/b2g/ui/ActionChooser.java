package org.nbp.b2g.ui;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class ActionChooser {
  private static String makeText (List<Action> actions, KeyBindingMap map, Integer cursorKey) {
    boolean haveCursorKey = cursorKey != null;

    StringBuilder sb = new StringBuilder();
    sb.append(ApplicationContext.getString(R.string.ChooseAction_label));

    for (Integer keys : map.keySet()) {
      boolean needsCursorKey = (keys & KeyMask.CURSOR) != 0;
      if (needsCursorKey != haveCursorKey) continue;

      Action action = map.get(keys);
      if (action.isHidden()) continue;
      if (action.isForDevelopers() && !ApplicationSettings.DEVELOPER_ENABLED) continue;

      sb.append('\n');
      sb.append(Wordify.get(action.getName()));

      sb.append(": ");
      sb.append(KeyMask.toString(keys));

      sb.append(": ");
      sb.append(action.getSummary());

      actions.add(action);
    }

    return sb.toString();
  }

  public static void chooseAction (KeyBindingMap map, final Integer cursorKey) {
    final List<Action> actions = new ArrayList<Action>();
    Endpoints.setPopupEndpoint(makeText(actions, map, cursorKey),
      new PopupClickHandler () {
        @Override
        public final boolean handleClick (int index) {
          if ((index -= 1) < 0) return true;
          Action action = actions.get(index);

          Endpoints.setHostEndpoint();
          if (cursorKey != null) return KeyEvents.performAction(action, cursorKey);
          return KeyEvents.performAction(action);
        }
      }
    );
  }

  public static void chooseAction (final KeyBindingMap map) {
    chooseAction(map, null);
  }

  public ActionChooser () {
  }
}
