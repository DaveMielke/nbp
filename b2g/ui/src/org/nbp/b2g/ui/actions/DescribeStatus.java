package org.nbp.b2g.ui.actions;
import org.nbp.b2g.ui.*;

import android.content.Context;
import android.os.Bundle;
import android.os.BatteryManager;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;

public class DescribeStatus extends Action {
  private static void appendString (StringBuilder sb, int string) {
    sb.append(ApplicationContext.getString(string));
  }

  private static void startLine (StringBuilder sb, int label) {
    if (sb.length() > 0) sb.append('\n');
    appendString(sb, label);
    sb.append(": ");
  }

  private static void addBatteryStatus (StringBuilder sb) {
    Bundle battery = HostMonitor.getBatteryStatus();

    if (battery != null) {
      startLine(sb, R.string.describeStatus_battery_label);

      {
        int level = battery.getInt(BatteryManager.EXTRA_LEVEL);
        int scale = battery.getInt(BatteryManager.EXTRA_SCALE);

        if (scale > 0) {
          sb.append(' ');
          sb.append(Integer.toString((level * 100) / scale));
          sb.append('%');
        }
      }
    }
  }

  private static void addWifiStatus (StringBuilder sb) {
    WifiManager wifi = (WifiManager)ApplicationContext.getSystemService(Context.WIFI_SERVICE);

    if (wifi != null) {
      startLine(sb, R.string.describeStatus_wifi_label);

      switch (wifi.getWifiState()) {
        default:
        case WifiManager.WIFI_STATE_UNKNOWN:
          appendString(sb, R.string.describeStatus_wifi_state_unknown);
          break;

        case WifiManager.WIFI_STATE_DISABLING:
          appendString(sb, R.string.describeStatus_wifi_state_disabling);
          break;

        case WifiManager.WIFI_STATE_DISABLED:
          appendString(sb, R.string.describeStatus_wifi_state_disabled);
          break;

        case WifiManager.WIFI_STATE_ENABLING:
          appendString(sb, R.string.describeStatus_wifi_state_enabling);
          break;

        case WifiManager.WIFI_STATE_ENABLED: {
          WifiInfo info = wifi.getConnectionInfo();

          if (info != null) {
            String name = info.getSSID();

            if (name != null) {
              sb.append(name);

              int dbm = info.getRssi();
              sb.append(' ');
              sb.append(wifi.calculateSignalLevel(dbm, 100));
              sb.append('%');

              sb.append(' ');
              sb.append(info.getLinkSpeed());
              sb.append("Mbps");
            }
          } else {
            appendString(sb, R.string.describeStatus_wifi_state_enabled);
          }

          break;
        }
      }
    }
  }

  @Override
  public boolean performAction () {
    StringBuilder sb = new StringBuilder();

    addBatteryStatus(sb);
    addWifiStatus(sb);

    if (sb.length() == 0) return false;
    Endpoints.setPopupEndpoint(sb.toString());
    return true;
  }

  public DescribeStatus (Endpoint endpoint) {
    super(endpoint, false);
  }
}
