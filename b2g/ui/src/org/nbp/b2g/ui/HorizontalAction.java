package org.nbp.b2g.ui;

public abstract class HorizontalAction extends DirectionalAction {
  @Override
  public ActionResult performInternalAction (Endpoint endpoint) {
    return performCursorAction(endpoint);
  }

  private final int getInternalPosition (Endpoint endpoint) {
    return endpoint.getLineStart() + endpoint.getLineIndent();
  }

  protected final int getSelectionStart (Endpoint endpoint) {
    return endpoint.isInputArea()?
           endpoint.getSelectionStart():
           getInternalPosition(endpoint);
  }

  protected final int getSelectionEnd (Endpoint endpoint) {
    return endpoint.isInputArea()?
           endpoint.getSelectionEnd():
           getInternalPosition(endpoint);
  }

  protected final ActionResult setCursor (Endpoint endpoint, int offset) {
    endpoint.setLine(offset);

    {
      CharSequence text = endpoint.getText();
      int end = findNextObject(endpoint, offset);
      if (end == NOT_FOUND) end = text.length();
      endpoint.setSelectedObject(offset, end);
    }

    if (endpoint.isInputArea()) {
      endpoint.setCursor(offset);
      return ActionResult.DONE;
    } else {
      endpoint.setLineIndent(offset - endpoint.getLineStart());
      return ActionResult.WRITE;
    }
  }

  protected final static int NOT_FOUND = -1;
  protected abstract int findNextObject (Endpoint endpoint, int offset);
  protected abstract int findPreviousObject (Endpoint endpoint, int offset);

  protected final ActionResult performNextAction (Endpoint endpoint) {
    int current = getSelectionEnd(endpoint);
    if (current != getSelectionStart(endpoint)) current -= 1;
    int next = findNextObject(endpoint, current);

    if (next == NOT_FOUND) return ActionResult.FAILED;
    return setCursor(endpoint, next);
  }

  protected final ActionResult performPreviousAction (Endpoint endpoint) {
    int current = getSelectionStart(endpoint);
    int previous = findPreviousObject(endpoint, current);

    if (previous == NOT_FOUND) return ActionResult.FAILED;
    return setCursor(endpoint, previous);
  }

  private final CharSequence getCurrentObject (Endpoint endpoint) {
    CharSequence text = endpoint.getText();
    int length = text.length();

    int start = getSelectionStart(endpoint);
    int end = getSelectionEnd(endpoint);

    if (start == end) {
      {
        if (start < length) start += 1;
        int previous = findPreviousObject(endpoint, start);
        if (previous != NOT_FOUND) start = previous;
      }

      {
        int next = findNextObject(endpoint, end);
        if (next == NOT_FOUND) next = length;
        end = next;
      }

      while (start < end) {
        if (!Character.isWhitespace(text.charAt(start))) break;
        start += 1;
      }

      while (end > start) {
        if (!Character.isWhitespace(text.charAt(--end))) {
          end += 1;
          break;
        }
      }

      if (start == end) return null;
    }

    return text.subSequence(start, end);
  }

  protected final ActionResult performSayAction (Endpoint endpoint) {
    CharSequence text = getCurrentObject(endpoint);
    if (text == null) return ActionResult.FAILED;

    ApplicationUtilities.say(text);
    return ActionResult.DONE;
  }

  protected final ActionResult performSpellAction (Endpoint endpoint) {
    CharSequence text = getCurrentObject(endpoint);
    if (text == null) return ActionResult.FAILED;

    int count = text.length();
    CharSequence[] characters = new CharSequence[count];

    for (int index=0; index<count; index+=1) {
      characters[index] = ApplicationUtilities.toString(text.charAt(index));
    }

    ApplicationUtilities.say(characters);
    return ActionResult.DONE;
  }

  protected HorizontalAction (Endpoint endpoint, boolean isAdvanced) {
    super(endpoint, isAdvanced);
  }
}
