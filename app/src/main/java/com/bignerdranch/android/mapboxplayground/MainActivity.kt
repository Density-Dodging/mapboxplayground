package com.bignerdranch.android.mapboxplayground

// geo json

import android.R.style
import android.graphics.Color
import android.graphics.Color.parseColor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource


private const val CIRCLE_LAYER_ID = "CIRCLE_LAYER_ID"
private const val LINE_LAYER_ID = "LINE_LAYER_ID"
private const val SOURCE_ID = "SOURCE_ID"
private const val MARKER_ICON_ID = "MARKER_ICON_ID"
private const val PROPERTY_ID = "PROPERTY_ID"
private const val PROPERTY_SELECTED = "PROPERTY_SELECTED"

class MainActivity : AppCompatActivity() {
    private var mapView: MapView? = null
    private var featureCollection: FeatureCollection? = null
    private lateinit var lineOne: FeatureCollection
    private lateinit var lineTwo: FeatureCollection
    private var buildingMarkers: MapRepository = MapRepository.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)


        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->

            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments

                // MARKERS
                // init marker positions
                initFeatureCollection();

                it.addSource(GeoJsonSource(SOURCE_ID, featureCollection))

                // Add the CircleLayer
                val circleLayer: CircleLayer = CircleLayer(CIRCLE_LAYER_ID, SOURCE_ID)
                    .withProperties(
                        circleRadius(
                            interpolate(
                                linear(), zoom(),
                                stop(2, 20f),
                                stop(3, 15f)
                            )
                        ),
                        circleColor(parseColor("#2196F3"))
                    )
                circleLayer.setFilter(eq(get(PROPERTY_SELECTED), literal(false)));
                it.addLayer(circleLayer)


                // LINES
                generateLineOne()
                drawLines(lineOne, it)

                // 3D - BUILDINGS
                val fillExtrusionLayer = FillExtrusionLayer("3d-buildings", "composite")
                fillExtrusionLayer.sourceLayer = "building"
                fillExtrusionLayer.setFilter(eq(get("extrude"), "true"))
                fillExtrusionLayer.minZoom = 15f
                fillExtrusionLayer.setProperties(
                    fillExtrusionColor(Color.LTGRAY),
                    fillExtrusionHeight(
                        interpolate(
                            exponential(1f),
                            zoom(),
                            stop(15, literal(0)),
                            stop(16, get("height"))
                        )
                    ),
                    fillExtrusionBase(get("min_height")),
                    fillExtrusionOpacity(0.6f)
                )
                it.addLayer(fillExtrusionLayer)
            }
        }
    }

    private fun initFeatureCollection() {
        val markerCoordinates: MutableList<Feature> = ArrayList()

        val featureOne = Feature.fromGeometry(
            Point.fromLngLat(-71.809658, 42.273796)
        )
        featureOne.addStringProperty(PROPERTY_ID, "1")
        featureOne.addBooleanProperty(PROPERTY_SELECTED, false)
        markerCoordinates.add(featureOne)
        val featureTwo = Feature.fromGeometry(
            Point.fromLngLat(-71.809036, 42.273252)
        )
        featureTwo.addStringProperty(PROPERTY_ID, "2")
        featureTwo.addBooleanProperty(PROPERTY_SELECTED, false)
        markerCoordinates.add(featureTwo)
        val featureThree = Feature.fromGeometry(
            Point.fromLngLat(-71.809894, 42.273232)
        )
        featureThree.addStringProperty(PROPERTY_ID, "3")
        featureThree.addBooleanProperty(PROPERTY_SELECTED, false)
        markerCoordinates.add(featureThree)
        featureCollection = FeatureCollection.fromFeatures(markerCoordinates)
    }

    private fun generateLineOne() {
        val markerCoordinates: MutableList<Feature> = ArrayList()
        val featureOne = Feature.fromGeometry(
            Point.fromLngLat(-71.809658, 42.273796)
        )
        markerCoordinates.add(featureOne)
        val featureTwo = Feature.fromGeometry(
            Point.fromLngLat(-71.809036, 42.273252)
        )
        markerCoordinates.add(featureTwo)
        val featureThree = Feature.fromGeometry(
            Point.fromLngLat(-71.809894, 42.273232)
        )
        markerCoordinates.add(featureThree)
        lineOne = FeatureCollection.fromFeatures(markerCoordinates)
    }

    private fun drawLines(featureCollection: FeatureCollection, style: Style) {
        style.addSource(
            GeoJsonSource("line-source", featureCollection,
            GeoJsonOptions().withLineMetrics(true))
        )

        // The layer properties for our line. This is where we make the line dotted, set the
        // color, etc.
        style.addLayer(
            LineLayer("linelayer", "line-source")
                .withProperties(
                    lineCap(Property.LINE_CAP_ROUND),
                    lineJoin(Property.LINE_JOIN_ROUND),
                    lineOpacity(.7f),
                    lineWidth(20f),
                    lineColor(parseColor("#3bb2d0"))
                )
        )
    }
}

