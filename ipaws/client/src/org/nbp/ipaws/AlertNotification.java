package org.nbp.ipaws;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.graphics.BitmapFactory;

import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.Activity;
import android.app.Service;

import org.nbp.common.CommonUtilities;

public abstract class AlertNotification extends ApplicationComponent {
  private final static String LOG_TAG = AlertNotification.class.getName();

  private AlertNotification () {
    super();
  }

  private final static Integer NOTIFICATION_IDENTIFIER = 1;
  private final static String NOTIFICATION_CHANNEL = "alert";

  private static NotificationManager notificationManager = null;
  private static Notification.Builder notificationBuilder = null;

  private static NotificationManager getManager () {
    if (notificationManager == null) {
      notificationManager = (NotificationManager)
                            getContext()
                           .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    return notificationManager;
  }

  private static PendingIntent newPendingIntent (Class<? extends Activity> activityClass) {
    Context context = getContext();
    Intent intent = new Intent(context, activityClass);

    intent.addFlags(
      Intent.FLAG_ACTIVITY_CLEAR_TASK |
      Intent.FLAG_ACTIVITY_NEW_TASK
    );

    return PendingIntent.getActivity(context, 0, intent, 0);
  }

  private static void makeBuilder () {
    Context context = getContext();

    if (CommonUtilities.haveOreo) {
      NotificationManager manager = getManager();
      NotificationChannel channel = manager.getNotificationChannel(NOTIFICATION_CHANNEL);

      if (channel == null) {
        channel = new NotificationChannel(
          NOTIFICATION_CHANNEL,
          getString(R.string.alert_channel_name),
          NotificationManager.IMPORTANCE_DEFAULT
        );

        manager.createNotificationChannel(channel);
      }

      notificationBuilder = new Notification.Builder(context, NOTIFICATION_CHANNEL);
    } else {
      notificationBuilder = new Notification.Builder(context)
        .setPriority(Notification.PRIORITY_DEFAULT)
        ;
    }

    notificationBuilder
      .setOngoing(true)
      .setOnlyAlertOnce(true)
      .setSmallIcon(R.drawable.alert_notification)
      .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.nbp_ipaws))
      .setContentTitle(getString(R.string.app_name))
      .setContentIntent(newPendingIntent(MainActivity.class))
      ;

    if (CommonUtilities.haveJellyBeanMR1) {
      notificationBuilder.setShowWhen(true);
    }

    if (CommonUtilities.haveLollipop) {
      notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
      notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
    }
  }

  private static boolean haveBuilder () {
    return notificationBuilder != null;
  }

  private static Notification buildNotification () {
    return notificationBuilder.build();
  }

  private static void refreshNotification () {
    getManager().notify(NOTIFICATION_IDENTIFIER, buildNotification());
  }

  private static void setSessionState (int state, String detail) {
    String text = getString(state);

    if ((detail != null) && !detail.isEmpty()) {
      text += ": " + detail;
    }

    notificationBuilder.setContentText(text);
    MainActivity.setStateText(text);
  }

  public static void updateSessionState (int state, String detail) {
    synchronized (NOTIFICATION_IDENTIFIER) {
      if (haveBuilder()) {
        setSessionState(state, detail);
        refreshNotification();
      }
    }
  }

  public static void updateSessionState (int state) {
    updateSessionState(state, null);
  }

  private static void setAlertCount () {
    int count = Alerts.list(false).length;
    notificationBuilder.setSubText(getResources().getQuantityString(R.plurals.alert, count, count));
    notificationBuilder.setWhen(System.currentTimeMillis());
  }

  public static void updateAlertCount () {
    synchronized (NOTIFICATION_IDENTIFIER) {
      if (haveBuilder()) {
        setAlertCount();
        refreshNotification();
      }
    }
  }

  private static boolean create (boolean refresh) {
    synchronized (NOTIFICATION_IDENTIFIER) {
      if (haveBuilder()) return false;

      makeBuilder();
      setSessionState(R.string.session_stateOff, null);
      setAlertCount();

      if (refresh) refreshNotification();
      return true;
    }
  }

  public static void create () {
    create(true);
  }

  public static void create (Service service) {
    synchronized (NOTIFICATION_IDENTIFIER) {
      create(false);
      service.startForeground(NOTIFICATION_IDENTIFIER, buildNotification());
    }
  }
}
