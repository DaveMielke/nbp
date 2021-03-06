package org.nbp.b2g.ui;

import android.util.Log;

import android.text.SpannableStringBuilder;

public abstract class DeleteAction extends Action {
  private final static String LOG_TAG = DeleteAction.class.getName();

  protected abstract int getDeleteOffset ();

  @Override
  public boolean performAction () {
    Endpoint endpoint = getEndpoint();

    synchronized (endpoint) {
      if (endpoint.isInputArea()) {
        int start = endpoint.getSelectionStart();
        int end = endpoint.getSelectionEnd();

        if (Endpoint.isSelected(start) && endpoint.isSelected(end)) {
          if (start == end) {
            start += getDeleteOffset();
            end = start + 1;
          }

          if (start < 0) return false;
          if (start >= endpoint.getTextLength()) return false;

          if (!ApplicationSettings.LITERARY_BRAILLE) {
            if (ApplicationSettings.LOG_ACTIONS) {
              Log.v(LOG_TAG,
                String.format(
                  "deleting text: %s", endpoint.getText().subSequence(start, end)
                )
              );
            }

            return endpoint.deleteText(start, end);
          }

          endpoint.adjustScroll(start);

          {
            int lineLength = endpoint.getLineLength();
            int lineStart = endpoint.getLineStart();

            {
              int from = lineStart + lineLength;

              if (from < end) {
                if (!endpoint.deleteText(from, end)) return false;
                endpoint.adjustScroll(start);
              }
            }

            start -= lineStart;
            end = Math.min((end - lineStart), lineLength);
            if (end == start) return true;
          }

          start = endpoint.findFirstBrailleOffset(start);
          end = endpoint.findEndBrailleOffset(end);

          CharSequence braille = endpoint.isHintText()? "": endpoint.getBrailleCharacters();
          SpannableStringBuilder sb = new SpannableStringBuilder(braille);
          sb.delete(start, end);
          return endpoint.setBrailleCharacters(sb, start);
        }
      }
    }

    return false;
  }

  protected DeleteAction (Endpoint endpoint) {
    super(endpoint, false);
  }
}
