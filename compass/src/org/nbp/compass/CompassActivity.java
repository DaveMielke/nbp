package org.nbp.compass;

import android.util.Log;

import android.app.Activity;
import android.os.Bundle;

import android.widget.TextView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CompassActivity extends Activity implements SensorEventListener {
  private final static String LOG_TAG = CompassActivity.class.getName();

  private SensorManager sensorManager;
  private TextView headingView;

  @Override
  public void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

    setContentView(R.layout.compass);
    headingView = (TextView)findViewById(R.id.heading);
  }

  @Override
  protected void onResume () {
    super.onResume();

    sensorManager.registerListener(this,
      sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
      R.integer.frequency_usecs
    );
  }

  @Override
  protected void onPause () {
    super.onPause();

    sensorManager.unregisterListener(this);
  }

  @Override
  public void onSensorChanged (SensorEvent event) {
    int degrees = Math.round(event.values[0]);

Log.d(LOG_TAG, String.format("%f %f %f", event.values[0], event.values[1], event.values[2]));
    headingView.setText(String.format(
      "%d",
      degrees
    ));
  }

  @Override
  public void onAccuracyChanged (Sensor sensor, int accuracy) {
  }
}