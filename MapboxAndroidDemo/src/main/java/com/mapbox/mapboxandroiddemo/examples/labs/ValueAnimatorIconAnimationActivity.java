package com.mapbox.mapboxandroiddemo.examples.labs;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconTranslate;

/**
 * Combine SymbolLayer icons with the Android system's ValueAnimator and interpolator
 * animation for a fun pin drop effect. The interpolator movement can also be used with other
 * types of map layers, such as a LineLayer or CircleLayer.
 * <p>
 * More info about https://developer.android.com/reference/android/view/animation/Interpolator
 */
public class ValueAnimatorIconAnimationActivity extends AppCompatActivity implements
  OnMapReadyCallback, MapView.OnDidFinishRenderingMapListener,AdapterView.OnItemSelectedListener {

  private static final String ICON_ID = "red-pin-icon-id";
  private static final String TAG = "PinDropActivity";

  // This float's actual value will depend on the height of the SymbolLayer icon
  private static final float DEFAULT_DESIRED_ICON_OFFSET = -16;
  private static final float STARTING_DROP_HEIGHT = -100;
  private static final long DROP_SPEED_MILLISECONDS = 2000;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private SymbolLayer pinSymbolLayer;
  private boolean animationHasStarted;
  private TimeInterpolator currentSelectedTimeInterpolator;
  private int counter;
  private boolean firstRunThrough;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    currentSelectedTimeInterpolator = new BounceInterpolator();
    firstRunThrough = true;

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_animated_pin_drop);

    // Initialize the map view
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapView.addOnDidFinishRenderingMapListener(this);
    addDataToMap();
  }

  /**
   * Implementing this interface so that animation only starts once all tiles have been loaded
   *
   * @param fully whether or not the mapy is finished rendering
   */
  @Override
  public void onDidFinishRenderingMap(boolean fully) {
    initAnimation(currentSelectedTimeInterpolator);
    initSpinnerMenu();
  }

  /**
   * Add images to the map so that the SymbolLayers can reference the images.
   */
  private void initLayerIcon() {
    Bitmap bluePinIcon = BitmapUtils.getBitmapFromDrawable(
      getResources().getDrawable(R.drawable.map_marker_push_pin_pink));
    mapboxMap.addImage(ICON_ID, bluePinIcon);
  }

  /**
   * Add the GeoJsonSource and SymbolLayers to the map.
   */
  private void addDataToMap() {
    initLayerIcon();
    initDataSource();
  }

  /**
   * Add GeoJsonSource with Features to the map.
   */
  private void initDataSource() {
    // Add a new source from the GeoJSON data
    mapboxMap.addSource(
      new GeoJsonSource("source-id" + counter,
        FeatureCollection.fromFeatures(new Feature[] {
          Feature.fromGeometry(Point.fromLngLat(
            119.86083984375,
            -1.834403324493515)),
          Feature.fromGeometry(Point.fromLngLat(
            116.06637239456177,
            5.970619502704659)),
          Feature.fromGeometry(Point.fromLngLat(
            114.58740234375,
            4.54357027937176)),
          Feature.fromGeometry(Point.fromLngLat(
            118.19091796875,
            5.134714634014467)),
          Feature.fromGeometry(Point.fromLngLat(
            110.36865234374999,
            1.4500404973608074)),
          Feature.fromGeometry(Point.fromLngLat(
            109.40185546874999,
            0.3076157096439005)),
          Feature.fromGeometry(Point.fromLngLat(
            115.79589843749999,
            1.5159363834516861)),
          Feature.fromGeometry(Point.fromLngLat(
            113.291015625,
            -0.9667509997666298)),
          Feature.fromGeometry(Point.fromLngLat(
            116.40083312988281,
            -0.3392008994314591))
        })
      )
    );
  }

  /**
   * Initialize and start the animation.
   *
   * @param desiredTimeInterpolator the type of Android system movement to animate the
   *                                SymbolLayer icons with.
   */
  private void initAnimation(TimeInterpolator desiredTimeInterpolator) {
    ValueAnimator animator = ValueAnimator.ofFloat(STARTING_DROP_HEIGHT, 0);
    animator.setDuration(DROP_SPEED_MILLISECONDS);
    animator.setInterpolator(desiredTimeInterpolator);
    animator.setStartDelay(1000);
    animator.start();
    animator.addUpdateListener(valueAnimator -> {
      if (!animationHasStarted) {
        initSymbolLayer();
        animationHasStarted = true;
      }
      pinSymbolLayer.setProperties(iconTranslate(new Float[]{0f,
        (Float) valueAnimator.getAnimatedValue()}));
    });
  }

  /**
   * Add the SymbolLayer to the map
   */
  private void initSymbolLayer() {
    pinSymbolLayer = new SymbolLayer("symbol-layer-id" + counter,
        "source-id" + counter);
    pinSymbolLayer.setProperties(
      iconImage(ICON_ID),
      iconIgnorePlacement(true),
      iconAllowOverlap(true),
      iconOffset(new Float[] {0f, DEFAULT_DESIRED_ICON_OFFSET}));
    mapboxMap.addLayer(pinSymbolLayer);
  }

  /**
   * Initialize the interpolator selection spinner menu
   */
  private void initSpinnerMenu() {
    Spinner interpolatorSelectionSpinnerMenu = findViewById(
        R.id.interpolator_selection_spinner_menu);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
      android.R.layout.simple_spinner_item, new String[] {
        getString(R.string.bounce_interpolator),
        getString(R.string.linear_interpolator),
        getString(R.string.accelerate_interpolator),
        getString(R.string.decelerate_interpolator)});
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    interpolatorSelectionSpinnerMenu.setAdapter(adapter);
    interpolatorSelectionSpinnerMenu.setOnItemSelectedListener(this);
  }

  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int position, long lon) {
    String selection = adapterView.getItemAtPosition(position).toString();
    if (selection.equals(getString(R.string.bounce_interpolator))) {
      if (firstRunThrough) {
        firstRunThrough = true;
      }
      currentSelectedTimeInterpolator = new BounceInterpolator();
    } else if (selection.equals(getString(R.string.linear_interpolator))) {
      currentSelectedTimeInterpolator = new LinearInterpolator();
      firstRunThrough = false;
    } else if (selection.equals(getString(R.string.accelerate_interpolator))) {
      currentSelectedTimeInterpolator = new AccelerateInterpolator();
      firstRunThrough = false;
    } else if (selection.equals(getString(R.string.decelerate_interpolator))) {
      currentSelectedTimeInterpolator = new DecelerateInterpolator();
      firstRunThrough = false;
    }
    if (!firstRunThrough) {
      animationHasStarted = false;
      mapboxMap.removeLayer("symbol-layer-id" + counter);
      counter++;
      addDataToMap();
      initAnimation(currentSelectedTimeInterpolator);
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {
    // Left empty on purpose
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
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
    mapView.onDestroy();
  }
}