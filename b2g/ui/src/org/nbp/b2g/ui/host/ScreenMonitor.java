package org.nbp.b2g.ui.host;
import org.nbp.b2g.ui.*;
import org.nbp.b2g.ui.host.HostEndpoint;

import java.util.Collection;
import java.util.List;

import org.nbp.common.Timeout;
import org.nbp.common.SettingsUtilities;

import android.util.Log;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityEvent;

import android.content.Intent;
import android.app.Notification;

import android.text.TextUtils;
import android.text.SpannableStringBuilder;

public class ScreenMonitor extends AccessibilityService {
  private final static String LOG_TAG = ScreenMonitor.class.getName();

  private static ScreenMonitor screenMonitor = null;

  public static void start () {
    SettingsUtilities.enableAccessibilityService(ScreenMonitor.class);
  }

  public static ScreenMonitor getScreenMonitor () {
    if (screenMonitor == null) Log.w(LOG_TAG, "screen monitor not runnig");
    return screenMonitor;
  }

  private static HostEndpoint getHostEndpoint () {
    return Endpoints.host.get();
  }

  private static CharSequence toText (Collection<CharSequence> lines) {
    if (lines == null) return null;
    if (lines.isEmpty()) return null;
    SpannableStringBuilder sb = new SpannableStringBuilder();

    for (CharSequence line : lines) {
      if (line == null) continue;
      if (sb.length() > 0) sb.append('\n');
      sb.append(line);
    }

    return sb.subSequence(0, sb.length());
  }

  private static boolean showText (
    CharSequence text,
    PopupClickHandler clickHandler,
    String... labels
  ) {
    if (text == null) return false;

    if (labels.length > 0) {
      StringBuilder sb = new StringBuilder();

      for (String label : labels) {
        if (!label.isEmpty()) {
          if (sb.length() > 0) sb.append(" - ");
          sb.append(label);
        }
      }

      if (sb.length() > 0) {
        sb.append('\n');
        sb.append(text);
        text = sb;
      }
    }

    return Endpoints.setPopupEndpoint(text.toString(), clickHandler);
  }

  private final boolean showText (
    Collection<CharSequence> lines,
    PopupClickHandler clickHandler,
    String... labels
  ) {
    return showText(toText(lines), clickHandler, labels);
  }

  private static CharSequence getAccessibilityText (AccessibilityNodeInfo node, AccessibilityEvent event) {
    if (node.getText() != null) return null;
    if (event == null) return null;

    {
      AccessibilityNodeInfo source = event.getSource();
      if (source == null) return null;

      boolean same = source.equals(node);
      source.recycle();
      if (!same) return null;
    }

    if (node.isPassword()) {
      CharSequence text = toText(event.getText());
      if (text != null) return text;

      if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
        int count = event.getItemCount();

        if (count != -1) {
          StringBuilder sb = new StringBuilder();
          char character = ApplicationParameters.PASSWORD_CHARACTER;

          while (count > 0) {
            sb.append(character);
            count -= 1;
          }

          return sb.toString();
        }
      }
    }

    return null;
  }

  private static boolean setAccessibilityText (AccessibilityNodeInfo node, AccessibilityEvent event) {
    CharSequence text = getAccessibilityText(node, event);
    if (text == null) return false;

    if (TextUtils.equals(text, AccessibilityText.get(node))) return false;
    AccessibilityText.set(node, text);
    return true;
  }

  private static boolean write (AccessibilityNodeInfo node, boolean force) {
    return getHostEndpoint().write(node, force);
  }

  private static boolean write (int string) {
    return getHostEndpoint().write(string);
  }

  @Override
  public void onCreate () {
    super.onCreate();

    Log.d(LOG_TAG, "screen monitor started");
    screenMonitor = this;

    ApplicationContext.setContext(this);
    write(R.string.message_no_screen_content);
  }

  @Override
  public void onDestroy () {
    try {
      screenMonitor = null;
      Log.d(LOG_TAG, "screen monitor stopped");
    } finally {
      super.onDestroy();
    }
  }

  @Override
  protected void onServiceConnected () {
    Log.d(LOG_TAG, "screen monitor connected");

    {
      AccessibilityNodeInfo node = ScreenUtilities.getCurrentNode();

      if (node != null) {
        write(node, true);
        node.recycle();
      }
    }
  }

  @Override
  public boolean onUnbind (Intent intent) {
    Log.d(LOG_TAG, "screen monitor disconnected");
    return false;
  }

  private static void appendProperty (StringBuilder sb, String label, String value) {
    if (value != null) {
      if (!value.isEmpty()) {
        if (sb.length() > 0) sb.append('\n');
        sb.append(label);
        sb.append(": ");
        sb.append(value);
      }
    }
  }

  private static void appendProperty (StringBuilder sb, String label, CharSequence value) {
    if (value != null) appendProperty(sb, label, value.toString());
  }

  private static void appendProperty (StringBuilder sb, String label, int value) {
    if (value != -1) appendProperty(sb, label, Integer.toString(value));
  }

  private static void appendProperty (StringBuilder sb, String label, Collection<CharSequence> value) {
    appendProperty(sb, label, toText(value));
  }

  private static void say (AccessibilityEvent event) {
    StringBuilder sb = new StringBuilder();

    {
      String string = event.toString();
      int index = string.indexOf(" TYPE_");

      string = string.substring(index+1);
      index = string.indexOf('_');

      string = string.substring(index+1);
      index = string.indexOf(' ');

      string = string.substring(0, index).replace('_', ' ').toLowerCase();
      appendProperty(sb, "Event", string);
    }

    appendProperty(sb, "Count", event.getItemCount());
    appendProperty(sb, "Current", event.getCurrentItemIndex());
    appendProperty(sb, "From", event.getFromIndex());
    appendProperty(sb, "To", event.getToIndex());
    appendProperty(sb, "Text", event.getText());

    if (sb.length() > 0) {
      Devices.speech.get().say(sb.toString());
    }
  }

  private static void logMissingEventComponent (String component) {
    if (ApplicationSettings.LOG_UPDATES) {
      Log.d(LOG_TAG, "no accessibility event " + component);
    }
  }

  private void logEventComponent (AccessibilityNodeInfo node, String description) {
    if (ApplicationSettings.LOG_UPDATES) {
      Log.d(LOG_TAG,  String.format(
        "accessibility event %s: %s",
        description, ScreenUtilities.toString(node)
      ));
    }
  }

  private static void setCurrentNode (AccessibilityEvent event) {
    AccessibilityNodeInfo root = event.getSource();

    if (root != null) {
      ScreenUtilities.logNavigation(root, "set event root");

      {
        int childIndex = event.getCurrentItemIndex();

        if (childIndex != -1) {
          int from = event.getFromIndex();
          if (from != -1) childIndex -= from;
        }

        if ((childIndex >= 0) && (childIndex < root.getChildCount())) {
          AccessibilityNodeInfo child = root.getChild(childIndex);

          if (child != null) {
            root.recycle();
            root = child;
            ScreenUtilities.logNavigation(root, "set event child");
          }
        }
      }

      {
        AccessibilityNodeInfo node = ScreenUtilities.findCurrentNode(root);

        if (node != null) {
          write(node, true);
          node.recycle();
        }
      }

      root.recycle();
    }
  }

  private String mostRecentAlert = null;

  private String toAlert (AccessibilityEvent event) {
    String alert = null;
    CharSequence text = toText(event.getText());

    if (text != null) {
      alert = text.toString();
    } else {
      int count = event.getItemCount();

      if (count != -1) {
        int index = event.getCurrentItemIndex();
        int percentage =
          (count == 0)? 0:
          ((index * 100) / count);

        alert = String.format("%d%%", percentage);
      }
    }

    if (alert == null) return null;
    if (alert.equals(mostRecentAlert)) return null;
    return (mostRecentAlert = alert);
  }

  private void handleNotification (AccessibilityEvent event) {
    if (ApplicationSettings.SHOW_NOTIFICATIONS) {
      Notification notification = (Notification)event.getParcelableData();

      int title;
      PopupClickHandler clickHandler;

      if (notification == null) {
        title = R.string.popup_type_alert;
        clickHandler = null;
      } else {
        title = R.string.popup_type_notification;

        clickHandler = new PopupClickHandler() {
          @Override
          public final boolean handleClick (int index) {
            if (!performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)) return false;
            return Endpoints.setHostEndpoint();
          }
        };
      }

      showText(event.getText(), clickHandler, getString(title));
    }
  }

  private void handleViewSelected (AccessibilityEvent event, AccessibilityNodeInfo view) {
    if (view == null) {
      String alert = toAlert(event);
      if (alert != null) ApplicationUtilities.message(alert.replace("\n", " "));
    } else if (ScreenUtilities.isBar(view)) {
      ScreenUtilities.logNavigation(view, String.format(
        "bar %d/%d", event.getCurrentItemIndex(), event.getItemCount()
      ));

      if (view.isAccessibilityFocused()) {
        String alert = toAlert(event);
        if (alert != null) getHostEndpoint().write(view, alert);
      }
    } else if (view.isFocused()) {
      setCurrentNode(event);
    }
  }

  private static void handleViewAccessibilityFocused (AccessibilityNodeInfo view) {
    if (view.isAccessibilityFocused()) write(view, true);
  }

  private static void handleViewInputFocused (AccessibilityEvent event, AccessibilityNodeInfo view) {
    AccessibilityNodeInfo inputFocus = null;

    if (view.isFocused())  {
      inputFocus = AccessibilityNodeInfo.obtain(view);
    } else if ((inputFocus = view.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)) != null) {
      ScreenUtilities.logNavigation(inputFocus, "view with input focus");
    } else {
      ScreenUtilities.logNavigation(view, "view not input focused");
    }

    if (inputFocus != null) {
      AccessibilityNodeInfo accessibilityFocus = inputFocus.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);

      if (accessibilityFocus != null) {
        ScreenUtilities.logNavigation(accessibilityFocus, "view with accessibility focus");
        accessibilityFocus.recycle();
      } else {
        setCurrentNode(event);
      }

      inputFocus.recycle();
    }
  }

  private static void handleViewScrolled (AccessibilityEvent event, AccessibilityNodeInfo view) {
    ScrollContainer container = ScrollContainer.getContainer(view);

    if (container != null) {
      synchronized (container) {
        container.setItemCount(event.getItemCount());
        container.setFirstItemIndex(event.getFromIndex());
        container.setLastItemIndex(event.getToIndex());
        container.onScroll();
      }
    }

    if (view.isFocused()) {
      setCurrentNode(event);
    }
  }

  private static void handleGranularityMovement (AccessibilityEvent event, AccessibilityNodeInfo view) {
    HostEndpoint endpoint = getHostEndpoint();

    synchronized (endpoint) {
      if (view == null) view = endpoint.getCurrentNode();

      if (view != null) {
        endpoint.write(view, toText(event.getText()));
        view.recycle();
      }
    }
  }

  private final static Object ACCESSIBILITY_EVENT_LOCK = new Object();

  private void handleAccessibilityEvent (AccessibilityEvent event) {
    int type = event.getEventType();
    AccessibilityNodeInfo source = event.getSource();

    switch (type) {
      case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
        break;

      case AccessibilityEvent.TYPE_VIEW_SELECTED:
        handleViewSelected(event, source);
        break;

      case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
        handleGranularityMovement(event, source);
        break;

      default:
        if (source != null) {
          logEventComponent(source, "source");
          setAccessibilityText(source, event);

          switch (type) {
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
              handleViewAccessibilityFocused(source);
              break;

            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
              handleViewInputFocused(event, source);
              break;

            default: {
              AccessibilityNodeInfo node = ScreenUtilities.findCurrentNode(source);

              switch (type) {
                case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                  handleViewScrolled(event, source);
                  break;
              }

              if (node != null) {
                logEventComponent(node, "node");

                switch (type) {
                  case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    write(node, true);
                    break;

                  default:
                    write(node, false);
                    break;

                  case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED: {
                    HostEndpoint endpoint = getHostEndpoint();
                    int cursor = event.getFromIndex() + event.getAddedCount();

                    synchronized (endpoint) {
                      boolean write = false;
                      if (endpoint.onTextChange(node)) write = true;
                      if (endpoint.onTextSelectionChange(source, cursor, cursor)) write = true;
                      if (write) endpoint.write();
                    }

                    break;
                  }

                  case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED: {
                    HostEndpoint endpoint = getHostEndpoint();
                    int from = event.getFromIndex();
                    int to = event.getToIndex();

                    synchronized (endpoint) {
                      if (endpoint.onTextSelectionChange(source, from, to)) {
                        write(node, false);
                      }
                    }

                    break;
                  }
                }

                node.recycle();
              } else {
                logMissingEventComponent("node");
              }

              break;
            }
          }

          source.recycle();
        } else {
          logMissingEventComponent("source");

          switch (type) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
              handleNotification(event);
              break;

            default:
              break;
          }
        }
        break;
    }
  }

  private final Timeout idleScreenDelay = new Timeout(ApplicationParameters.IDLE_SCREEN_DELAY, "idle-screen-delay") {
    @Override
    public void run () {
      synchronized (ACCESSIBILITY_EVENT_LOCK) {
        HostEndpoint endpoint = getHostEndpoint();

        synchronized (endpoint) {
          AccessibilityNodeInfo node = endpoint.getCurrentNode();

          if (node != null) {
            {
              AccessibilityNodeInfo refreshed = ScreenUtilities.getRefreshedNode(node);

              if (refreshed != null) {
                node.recycle();
                node = refreshed;
              }
            }

            logEventComponent(node, "reassessment");
            write(node, false);
            node.recycle();
          }
        }
      }
    }
  };

  @Override
  public void onAccessibilityEvent (final AccessibilityEvent event) {
    synchronized (idleScreenDelay) {
      idleScreenDelay.cancel();

      synchronized (ACCESSIBILITY_EVENT_LOCK) {
        if (ApplicationSettings.LOG_UPDATES) {
          Log.d(LOG_TAG, "accessibility event starting: " + event.toString());
        //say(event);
        }

        try {
          Crash.runComponent(
            "accessibility event", event.toString(),
            new Runnable() {
              @Override
              public void run () {
                handleAccessibilityEvent(event);
              }
            }
          );

          idleScreenDelay.start();
        } finally {
          if (ApplicationSettings.LOG_UPDATES) {
            Log.d(LOG_TAG, "accessibility event finished");
          }
        }
      }
    }
  }

  @Override
  public void onInterrupt () {
  }
}
