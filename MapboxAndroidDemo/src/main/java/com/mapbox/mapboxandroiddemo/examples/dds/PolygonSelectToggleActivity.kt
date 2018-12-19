package com.mapbox.mapboxandroiddemo.examples.dds

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxandroiddemo.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_dds_polygon_select_toggle.*

class PolygonSelectToggleActivity : AppCompatActivity() , MapboxMap.OnMapClickListener {

    private var mapboxMap: MapboxMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token))

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_dds_polygon_select_toggle)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap

            mapboxMap.setStyle(Style.LIGHT) {

                val geoJsonSource = GeoJsonSource(
                        "polygon-source",
                        FeatureCollection.fromJson(
                                loadGeoJsonFromAsset("new-orleans-neighborhoods.geojson")!!)
                )
                mapboxMap.style?.addSource(geoJsonSource)

                val fillLayer = FillLayer("fill-layer-id", "polygon-source")
                fillLayer.withProperties(
                        fillColor(Color.GRAY),
                        fillOpacity(.3f))
                mapboxMap.style?.addLayer(fillLayer)

                val lineLayer = LineLayer("layer-layer-id", "polygon-source")
                lineLayer.withProperties(
                        lineColor(Color.GRAY),
                        lineWidth(2f))
                mapboxMap.style?.addLayerBelow(lineLayer,"place-city-sm")

                mapboxMap.addOnMapClickListener(this)
            }
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        val pixel = mapboxMap?.getProjection()?.toScreenLocation(point)
        val features = mapboxMap?.queryRenderedFeatures(pixel!!,
                "fill-layer-id","layer-layer-id")

        if (features!!.size > 0) {
            val feature = features[0]
            Log.d("PolygonSelectToggle", "feature = " + feature)
        }

        return true
    }

    private fun loadGeoJsonFromAsset(filename: String): String? {
        try {
            // Load GeoJSON file
            val `is` = assets.open(filename)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            return String(buffer)

        } catch (exception: Exception) {
            Log.e("PolygonSelectToggle", "Exception Loading GeoJSON: " + exception.toString())
            exception.printStackTrace()
            return null
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