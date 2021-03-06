package org.nbp.b2g.ui.actions;
import org.nbp.b2g.ui.*;

public class TypeEmoticon extends InputAction {
  @Override
  protected final boolean performInputAction (final Endpoint endpoint) {
    StringBuilder message = new StringBuilder();
    message.append(getString(R.string.popup_select_emoticon));
    final String[] emoticons = EmoticonUtilities.getEmoticons();

    for (String emoticon : emoticons) {
      message.append('\n');
      message.append(EmoticonUtilities.getDescription(emoticon));
    }

    return Endpoints.setPopupEndpoint(message.toString(), 1,
      new PopupClickHandler() {
        @Override
        public boolean handleClick (int index) {
          StringBuilder emoticon = new StringBuilder(emoticons[index]);

          synchronized (endpoint) {
            CharSequence text = endpoint.getText();
            int start = endpoint.getSelectionStart();
            int end = endpoint.getSelectionEnd();

            if (start > 0) {
              if (!Character.isWhitespace(text.charAt(start-1))) {
                emoticon.insert(0, ' ');
              }
            }

            if (!((end < text.length()) && Character.isWhitespace(text.charAt(end)))) {
              emoticon.append(' ');
            }

            return endpoint.insertText(emoticon.toString());
          }
        }
      }
    );
  }

  @Override
  public boolean editsInput () {
    return true;
  }

  public TypeEmoticon (Endpoint endpoint) {
    super(endpoint);
  }
}
