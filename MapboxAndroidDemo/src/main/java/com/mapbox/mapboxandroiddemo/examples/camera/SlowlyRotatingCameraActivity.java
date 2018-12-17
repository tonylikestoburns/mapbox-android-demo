package com.mapbox.mapboxandroiddemo.examples.camera;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * Animate the map's camera to slowly spin around a single point ont the map.
 */
public class SlowlyRotatingCameraActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnMapClickListener {

  private static final int DESIRED_NUM_OF_SPINS = 5;
  private static final int DESIRED_SECONDS_PER_ONE_FULL_360_SPIN = 40;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private CameraPosition position;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_camera_slow_spin);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {

    SlowlyRotatingCameraActivity.this.mapboxMap = mapboxMap;

    // Toast instructing user to tap on the map
    Toast.makeText(
      SlowlyRotatingCameraActivity.this, getString(R.string.tap_on_map_instruction), Toast.LENGTH_LONG).show();

    mapboxMap.addOnMapClickListener(this);
    initAnimator(mapboxMap.getCameraPosition().target);
  }

  @Override
  public void onMapClick(@NonNull LatLng point) {
    initAnimator(point);
  }

  /**
   * Set up and start the spin animation. The Android system ValueAnimator emits a new value and that value is
   * used as the map camera's new bearing rotation amount. A smooth "new helicopter" type of effect is created
   * by using a LinearInterpolator.
   *
   * @param point the map location that the map camera should spin around
   */
  private void initAnimator(@NonNull LatLng point) {
    ValueAnimator animator = ValueAnimator.ofFloat(0, DESIRED_NUM_OF_SPINS * 360);
    animator.setDuration(
      DESIRED_NUM_OF_SPINS * DESIRED_SECONDS_PER_ONE_FULL_360_SPIN * 1000); // *1000 to convert to milliseconds
    animator.setInterpolator(new LinearInterpolator());
    animator.setStartDelay(1000);
    animator.start();
    animator.addUpdateListener(valueAnimator -> {

      // Retrieve the new animation number
      Float newBearingValue = (Float) valueAnimator.getAnimatedValue();

      // Use the animation number in a new camera position and then direct the map camera to move to the new position
      mapboxMap.animateCamera(CameraUpdateFactory
        .newCameraPosition(new CameraPosition.Builder()
          .target(new LatLng(point.getLatitude(), point.getLongitude()))
          .bearing(newBearingValue)
          .build()));
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mapboxMap != null) {
      mapboxMap.removeOnMapClickListener(this);
    }
    mapView.onDestroy();
  }
}
