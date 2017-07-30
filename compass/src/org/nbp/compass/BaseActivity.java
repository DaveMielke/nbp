package org.nbp.compass;

import java.util.List;
import java.io.IOException;

import org.nbp.common.CommonActivity;
import org.nbp.common.CommonUtilities;

import android.os.Build;
import android.util.Log;

import android.os.Bundle;
import android.os.AsyncTask;

import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;

import android.view.accessibility.AccessibilityManager;

import android.text.TextUtils;

import android.location.Location;
import android.location.Address;
import android.location.Geocoder;

public abstract class BaseActivity extends CommonActivity {
  private final static String LOG_TAG = BaseActivity.class.getName();

  private AccessibilityManager accessibilityManager;

  private final boolean isAccessibilityEnabled () {
    if (accessibilityManager == null) return false;
    return accessibilityManager.isEnabled();
  }

  // accuracy
  private TextView accuracySatellites;
  private TextView accuracyHorizontal;

  // address
  private TextView addressName;
  private TextView addressDistance;
  private TextView addressDirection;
  private TextView addressDegrees;
  private TextView addressPoint;

  // motion
  private TextView speedMagnitude;
  private TextView bearingDegrees;
  private TextView bearingPoint;

  // orientation
  private TextView headingDegrees;
  private TextView headingPoint;
  private ImageView headingCompass;
  private TextView pitchDegrees;
  private TextView rollDegrees;

  // position
  private TextView altitudeMagnitude;
  private TextView latitudeDecimal;
  private TextView latitudeDMS;
  private TextView longitudeDecimal;
  private TextView longitudeDMS;

  protected final void findViews () {
    // accuracy
    accuracySatellites = (TextView)findViewById(R.id.accuracy_satellites);
    accuracyHorizontal = (TextView)findViewById(R.id.accuracy_horizontal);

    // address
    addressName = (TextView)findViewById(R.id.address_name);
    addressDistance = (TextView)findViewById(R.id.address_distance);
    addressDirection = (TextView)findViewById(R.id.address_direction);
    addressDegrees = (TextView)findViewById(R.id.address_degrees);
    addressPoint = (TextView)findViewById(R.id.address_point);

    // motion
    speedMagnitude = (TextView)findViewById(R.id.speed_magnitude);
    bearingDegrees = (TextView)findViewById(R.id.bearing_degrees);
    bearingPoint = (TextView)findViewById(R.id.bearing_point);

    // orientation
    headingDegrees = (TextView)findViewById(R.id.heading_degrees);
    headingPoint = (TextView)findViewById(R.id.heading_point);
    headingCompass = (ImageView)findViewById(R.id.compass);
    pitchDegrees = (TextView)findViewById(R.id.pitch_degrees);
    rollDegrees = (TextView)findViewById(R.id.roll_degrees);

    // position
    altitudeMagnitude = (TextView)findViewById(R.id.altitude_magnitude);
    latitudeDecimal = (TextView)findViewById(R.id.latitude_decimal);
    latitudeDMS = (TextView)findViewById(R.id.latitude_dms);
    longitudeDecimal = (TextView)findViewById(R.id.longitude_decimal);
    longitudeDMS = (TextView)findViewById(R.id.longitude_dms);
  }

  private final CharSequence getText (TextView view) {
    if (view == null) return null;

    CharSequence text = view.getText();
    if (text == null) return null;

    if (text.length() == 0) return null;
    return text;
  }

  public final void announce (String announcement) {
    Announcements.say(announcement, true);
  }

  public final void announceLocation () {
    StringBuilder announcement = new StringBuilder();

    {
      CharSequence name = getText(addressName);

      if (name != null) {
        announcement.append(name);
        announcement.append(':');
      }
    }

    {
      CharSequence distance = getText(addressDistance);

      if (distance != null) {
        if (announcement.length() > 0) announcement.append(' ');
        announcement.append(distance);
      }
    }

    {
      CharSequence direction = getText(addressDirection);
      if (direction == null) direction = getText(addressPoint);

      if (direction != null) {
        if (announcement.length() > 0) announcement.append(' ');
        announcement.append(direction);
      }
    }

    if (announcement.length() > 0) announce(announcement.toString());
  }

  private final void rotateTo (View view, float degrees, String label) {
    if (view != null) {
      degrees = ApplicationUtilities.toNearestAngle(degrees, view.getRotation());
      view.animate().rotation(degrees).start();

      if (label != null) {
        view.setContentDescription(
          String.format(
            "[%s rotated @ %s]", label,
            ApplicationUtilities.toAngleText(ApplicationUtilities.toUnsignedAngle(degrees))
          )
        );
      }
    }
  }

  private final void rotateTo (View view, float degrees) {
    rotateTo(view, degrees, null);
  }

  private final DelayedAction getChangedTextAnnouncer (TextView view) {
    int key = R.string.text_tag_announcer;

    synchronized (view) {
      DelayedAction action = (DelayedAction)view.getTag(key);

      if (action == null) {
        action = new DelayedAction(
          ApplicationParameters.ANNOUNCE_MINIMUM_TIME, "announce-text"
        );

        view.setTag(key, action);
      }

      return action;
    }
  }

  private final void setText (final TextView view, CharSequence text) {
    if (view != null) {
      DelayedAction announcer = getChangedTextAnnouncer(view);

      synchronized (announcer) {
        synchronized (view) {
          if (!TextUtils.equals(text, view.getText())) {
            view.setText(text);

            announcer.setAction(
              new Runnable() {
                @Override
                public void run () {
                  synchronized (view) {
                    int key = R.string.text_tag_announcement;
                    final CharSequence text = view.getText();
                    boolean cancel = true;

                    if (isAccessibilityEnabled()) {
                      if (CommonUtilities.haveAndroidSDK(Build.VERSION_CODES.LOLLIPOP)) {
                        if (view.isAccessibilityFocused()) {
                          cancel = false;

                          if (text.length() > 0) {
                            if (!TextUtils.equals(text, (CharSequence)view.getTag(key))) {
                              accessibilityManager.interrupt();

                              CommonUtilities.runUnsafeCode(
                                new Runnable() {
                                  @Override
                                  public void run () {
                                    view.announceForAccessibility(text);
                                  }
                                }
                              );
                            }
                          }
                        }
                      }
                    }

                    view.setTag(key, (cancel? null: text));
                  }
                }
              }
            );
          }
        }
      }
    }
  }

  private final void setText (TextView view, int resource) {
    setText(view, getString(resource));
  }

  private final void setText (TextView view) {
    setText(view, "");
  }

  private final void setDistance (TextView view, double meters) {
    setText(view, ApplicationUtilities.toDistanceText(meters));
  }

  private final void setSpeed (TextView view, float metersPerSecond) {
    setText(view, ApplicationUtilities.toSpeedText(metersPerSecond));
  }

  private final void setAngle (TextView view, float degrees) {
    setText(view, ApplicationUtilities.toAngleText(degrees));
  }

  private final void setHeading (TextView view, float degrees) {
    setText(view, ApplicationUtilities.toHeadingText(degrees));
  }

  private final void setLatitude (TextView view, double degrees) {
    setText(view, ApplicationUtilities.toLatitudeText(degrees));
  }

  private final void setLongitude (TextView view, double degrees) {
    setText(view, ApplicationUtilities.toLongitudeText(degrees));
  }

  private final void setCoordinate (TextView view, double degrees) {
    setText(view, ApplicationUtilities.toCoordinateText(degrees));
  }

  private final void setPoint (TextView view, float degrees) {
    setText(view, ApplicationUtilities.toPointText(degrees));
  }

  private final static float UNKNOWN_VALUE = Float.NaN;
  private float orientationHeading = UNKNOWN_VALUE;
  private float addressHeading = UNKNOWN_VALUE;

  private final CharSequence toRelativeText (float direction) {
    float reference = orientationHeading;
    if (reference == UNKNOWN_VALUE) return null;

    if (direction == UNKNOWN_VALUE) return null;
    return ApplicationUtilities.toRelativeText(direction, reference);
  }

  private final void setRelativeDirection (TextView view, float direction) {
    CharSequence text = toRelativeText(direction);

    if (text != null) {
      setText(view, text);
    } else {
      setText(view);
    }
  }

  private final void setAddressDirection () {
    setRelativeDirection(addressDirection, addressHeading);
  }

  private final void setAddressName (CharSequence name) {
    setText(addressName, name);
  }

  private final void setAddressDistance (float distance) {
    if (distance != UNKNOWN_VALUE) {
      setDistance(addressDistance, distance);
    } else {
      setText(addressDistance);
    }
  }

  private final void setAddressHeading (float heading) {
    if ((addressHeading = heading) != UNKNOWN_VALUE) {
      setHeading(addressDegrees, heading);
      setPoint(addressPoint, heading);
    } else {
      setText(addressDegrees);
      setText(addressPoint);
    }

    setAddressDirection();
  }

  private Geocoder geocoder;

  private final void prepareGeocoding () {
    geocoder = Geocoder.isPresent()? new Geocoder(this): null;

    setText(addressName,
      (geocoder != null)?
      R.string.message_waiting:
      R.string.message_unsupported
    );
  }

  private boolean atNewLocation = false;
  private boolean amGeocodingLocation = false;

  private double locationLatitude = UNKNOWN_VALUE;
  private double locationLongitude = UNKNOWN_VALUE;

  private final void geocodeLocation () {
    synchronized (this) {
      if (!amGeocodingLocation) {
        amGeocodingLocation = true;

        new AsyncTask<Void, Object, Void>() {
          @Override
          protected void onProgressUpdate (Object... arguments) {
            CharSequence name = (CharSequence)arguments[0];
            Float distance    = (Float)       arguments[1];
            Float direction   = (Float)       arguments[2];

            setAddressDistance((distance != null)? distance: UNKNOWN_VALUE);
            setAddressHeading((direction != null)? ApplicationUtilities.toUnsignedAngle(direction): UNKNOWN_VALUE);

            if (!name.equals(getText(addressName))) {
              setAddressName(name);
              if (ApplicationSettings.ANNOUNCE_LOCATION) announceLocation();
            }
          }

          private final boolean geocode (double latitude, double longitude) {
            String problem = null;

            try {
              List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

              if (addresses != null) {
                if (!addresses.isEmpty()) {
                  Address address = addresses.get(0);

                  if (ApplicationSettings.LOG_GEOCODING) {
                    Log.d(LOG_TAG, ("address: " + AddressUtilities.toString(address)));
                  }

                  CharSequence name = AddressUtilities.getName(address);
                  if (name.length() == 0) name = getString(R.string.message_unknown);

                  Float distance = null;
                  Float direction = null;

                  if (address.hasLatitude() && address.hasLongitude()) {
                    float[] results = new float[2];

                    Location.distanceBetween(
                      latitude, longitude,
                      address.getLatitude(), address.getLongitude(),
                      results
                    );

                    distance = results[0];
                    direction = results[1];

                    if (ApplicationSettings.LOG_GEOCODING) {
                      StringBuilder sb = new StringBuilder("orientation:");

                      sb.append(' ');
                      sb.append(ApplicationUtilities.toCoordinatesText(latitude, longitude));

                      sb.append(' ');
                      sb.append(ApplicationUtilities.toDistanceText(distance));
                      sb.append('@');
                      sb.append(ApplicationUtilities.toAngleText(direction));

                      Log.d(LOG_TAG, sb.toString());
                    }
                  }

                  publishProgress(name, distance, direction);
                  return true;
                } else {
                  problem = "no addresses";
                }
              } else {
                problem = "no address list";
              }
            } catch (IOException exception) {
              problem = exception.getMessage();
            }

            Log.w(LOG_TAG, String.format(
              "geocoding failure: %s: %s",
              ApplicationUtilities.toCoordinatesText(latitude, longitude),
              problem
            ));

            return false;
          }

          @Override
          protected Void doInBackground (Void... arguments) {
            while (true) {
              double latitude;
              double longitude;

              synchronized (BaseActivity.this) {
                if (!atNewLocation) {
                  amGeocodingLocation = false;
                  return null;
                }

                latitude = locationLatitude;
                longitude = locationLongitude;
                atNewLocation = false;
              }

              geocode(latitude, longitude);
            }
          }
        }.execute();
      }
    }
  }

  private final void geocodeLocation (double latitude, double longitude) {
    if (geocoder != null) {
      synchronized (this) {
        locationLatitude = latitude;
        locationLongitude = longitude;
        atNewLocation = true;
        geocodeLocation();
      }
    }
  }

  private final void setPosition (double latitude, double longitude) {
    setCoordinate(latitudeDecimal, latitude);
    setCoordinate(longitudeDecimal, longitude);

    setLatitude(latitudeDMS, latitude);
    setLongitude(longitudeDMS, longitude);

    geocodeLocation(latitude, longitude);
  }

  public final void setLocation (Location location) {
    setPosition(location.getLatitude(), location.getLongitude());

    if (location.hasAltitude()) {
      double meters = location.getAltitude();
      setDistance(altitudeMagnitude, meters);
    } else {
      setText(altitudeMagnitude);
    }

    if (location.hasSpeed()) {
      float metersPerSecond = location.getSpeed();
      setSpeed(speedMagnitude, metersPerSecond);
    } else {
      setText(speedMagnitude);
    }

    if (location.hasBearing()) {
      float degrees = location.getBearing();
      setAngle(bearingDegrees, degrees);
      setPoint(bearingPoint, degrees);
    } else {
      setText(bearingDegrees);
      setText(bearingPoint);
    }

    if (location.hasAccuracy()) {
      float distance = location.getAccuracy();
      setText(accuracyHorizontal, ("±" + ApplicationUtilities.toDistanceText(distance)));
    } else {
      setText(accuracyHorizontal);
    }

    {
      String satelliteCount = "";
      Bundle extras = location.getExtras();

      if (extras != null) {
        {
          String key = "satellites";

          if (extras.containsKey(key)) {
            satelliteCount = Integer.toString(extras.getInt(key));
          }
        }
      }

      setText(accuracySatellites, satelliteCount);
    }
  }

  public final void setOrientationHeading (float heading) {
    setHeading(headingDegrees, heading);
    setPoint(headingPoint, heading);
    rotateTo(headingCompass, -heading, "compass");

    orientationHeading = heading;
    setAddressDirection();
  }

  public final void setOrientationPitch (float degrees) {
    setAngle(pitchDegrees, degrees);
  }

  public final void setOrientationRoll (float degrees) {
    setAngle(rollDegrees, degrees);
  }

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    accessibilityManager = (AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
  }

  protected final void finishBaseActivityCreation () {
    findViews();
    prepareGeocoding();
  }
}
