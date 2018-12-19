package com.mapbox.mapboxandroiddemo.examples.dds

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_dds_polygon_select_toggle.*
import java.lang.ref.WeakReference

class PolygonSelectToggleActivity : AppCompatActivity(), MapboxMap.OnMapClickListener {

    private var mapboxMap: MapboxMap? = null
    private val FILL_LAYER_ID = "FILL_LAYER_ID"
    private val LINE_LAYER_ID = "LINE_LAYER_ID"
    private val NEIGHBORHOOD_POLYGON_SOURCE_ID = "NEIGHBORHOOD_POLYGON_SOURCE_ID"
    private val PROPERTY_SELECTED = "selected"
    private val NEIGHBORHOOD_NAME_PROPERTY = "neighborhood_name"
    private var featureCollection: FeatureCollection? = null
    private var geoJsonSource: GeoJsonSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_dds_polygon_select_toggle)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.LIGHT) {

                this.mapboxMap = mapboxMap

                LoadGeoJsonDataTask(this).execute()
                mapboxMap.addOnMapClickListener(this)

                Toast.makeText(this, getString(R.string.tap_on_neighborhood),
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        return handleClickIcon(mapboxMap?.getProjection()!!.toScreenLocation(point))
    }

    /**
     * Sets up all of the sources and layers needed for this example
     *
     * @param collection the FeatureCollection to set equal to the globally-declared FeatureCollection
     */
    fun setUpData(collection: FeatureCollection) {
        if (mapboxMap == null) {
            return
        }
        featureCollection = collection

        geoJsonSource = GeoJsonSource(
                NEIGHBORHOOD_POLYGON_SOURCE_ID, featureCollection)

        mapboxMap?.style?.addSource(geoJsonSource!!)

        val neighborhoodPolygonFillLayer = FillLayer(FILL_LAYER_ID, NEIGHBORHOOD_POLYGON_SOURCE_ID)
        neighborhoodPolygonFillLayer.withProperties(
                fillColor(
                        match(get(literal(PROPERTY_SELECTED)),
                                literal(true),
                                stop(literal(true), Expression.color(Color.parseColor("#F38E39"))),
                                stop(literal(false), Expression.color(Color.parseColor("#39F3EA")))
                        )),
                fillOpacity(.15f))
        mapboxMap?.style?.addLayerBelow(neighborhoodPolygonFillLayer, "place-city-sm")

        val neighborhoodOutlineLineLayer = LineLayer(LINE_LAYER_ID, NEIGHBORHOOD_POLYGON_SOURCE_ID)
        neighborhoodOutlineLineLayer.withProperties(
                lineColor(Color.GRAY),
                lineWidth(2f))
        mapboxMap?.style?.addLayerBelow(neighborhoodOutlineLineLayer, "place-city-sm")
    }


    /**
     * This method handles click events for SymbolLayer symbols.

     * When a SymbolLayer icon is clicked, we moved that feature to the selected state.
     * @param screenPoint the point on screen clicked
     */
    private fun handleClickIcon(screenPoint: PointF): Boolean {
        val features = mapboxMap?.queryRenderedFeatures(screenPoint, FILL_LAYER_ID)
        if (features?.isEmpty() == false) {
            Toast.makeText(this, features[0].getStringProperty(NEIGHBORHOOD_NAME_PROPERTY),
                    Toast.LENGTH_SHORT).show()
            val name = features.get(0).getStringProperty(NEIGHBORHOOD_NAME_PROPERTY)
            val featureList = featureCollection?.features()
            for (i in featureList!!.indices) {
                if (featureList.get(i).getStringProperty(NEIGHBORHOOD_NAME_PROPERTY) == name) {
                    Log.d("PolygonSelectToggle", "" +
                            "featureList.get(i).getStringProperty(NEIGHBORHOOD_NAME_PROPERTY) == name")

                    if (featureSelectStatus(i)) {
                        setFeatureSelectState(featureList.get(i), false)
                    } else {
                        setSelected(i)
                    }
                }
            }
            return true
        } else {
            return false
        }
    }

    /**
     * Selects the state of a feature
     *
     * @param feature the feature to be selected.
     */
    private fun setFeatureSelectState(feature: Feature, selectedState: Boolean) {
        Log.d("PolygonSelectToggle", "setFeatureSelectState")
        Log.d("PolygonSelectToggle", "selectedState = " + selectedState)

        feature.properties()!!.addProperty(PROPERTY_SELECTED, selectedState)
        refreshSource()
    }

    /**
     * Checks whether a Feature's boolean "selected" property is true or false
     *
     * @param index the specific Feature's index position in the FeatureCollection's list of Features.
     * @return true if "selected" is true. False if the boolean property is false.
     */
    private fun featureSelectStatus(index: Int): Boolean {
        Log.d("PolygonSelectToggle", "featureSelectStatus returned "+
                featureCollection?.features()!![index].getBooleanProperty(PROPERTY_SELECTED))

        return if (featureCollection == null) {
            false
        } else featureCollection?.features()!![index].getBooleanProperty(PROPERTY_SELECTED)
    }

    /**
     * Updates the display of data on the map after the FeatureCollection has been modified
     */
    private fun refreshSource() {
        if (geoJsonSource != null && featureCollection != null) {
            geoJsonSource?.setGeoJson(featureCollection)
        }
    }

    /**
     * Set a feature selected state.
     *
     * @param index the index of selected feature
     */
    private fun setSelected(index: Int) {
        val feature = featureCollection?.features()!![index]
        setFeatureSelectState(feature, true)
        refreshSource()
    }

    /**
     * AsyncTask to load data from the assets folder.
     */
    private class LoadGeoJsonDataTask internal
    constructor(activity: PolygonSelectToggleActivity) :
            AsyncTask<Void, Void, FeatureCollection>() {

        private val PROPERTY_SELECTED = "selected"
        private val activityRef: WeakReference<PolygonSelectToggleActivity>

        init {
            this.activityRef = WeakReference(activity)
        }

        override fun doInBackground(vararg params: Void): FeatureCollection? {
            val activity = activityRef.get() ?: return null

            val geoJson = loadGeoJsonFromAsset(activity,
                    "new-orleans-neighborhoods.geojson")
            return FeatureCollection.fromJson(geoJson)
        }

        override fun onPostExecute(featureCollection: FeatureCollection?) {
            super.onPostExecute(featureCollection)
            val activity = activityRef.get()
            if (featureCollection == null || activity == null) {
                return
            }

            // This example runs on the premise that each GeoJSON Feature has a "selected" property,
            // with a boolean value. If your data's Features don't have this boolean property,
            // add it to the FeatureCollection 's features with the following code:
            for (singleFeature in featureCollection.features()!!) {
                singleFeature.addBooleanProperty(PROPERTY_SELECTED, false)
            }

            activity.setUpData(featureCollection)
        }

        companion object {

            internal fun loadGeoJsonFromAsset(context: Context, filename: String): String {
                try {
                    // Load GeoJSON file
                    val `is` = context.assets.open(filename)
                    val size = `is`.available()
                    val buffer = ByteArray(size)
                    `is`.read(buffer)
                    `is`.close()
                    return String(buffer)
                } catch (exception: Exception) {
                    throw RuntimeException(exception)
                }

            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}