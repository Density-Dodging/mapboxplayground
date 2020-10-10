package com.bignerdranch.android.mapboxplayground

// geo json

import android.graphics.Color
import android.graphics.Color.YELLOW
import android.graphics.Color.parseColor
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource


private const val GREEN_CIRCLE_LAYER_ID = "green_buildings"
private const val GREEN_BUILDINGS_SOURCE_ID = "green_buildings_source"

private const val YELLOW_CIRCLE_LAYER_ID = "yellow_buildings"
private const val YELLOW_BUILDINGS_SOURCE_ID = "yellow_buildings_source"

private const val RED_CIRCLE_LAYER_ID = "red_buildings"
private const val RED_BUILDINGS_SOURCE_ID = "red_buildings_source"

private const val BUILDING_NAME = "building_name"
private const val BUILDING_FLOORS = "building_floors"
private const val BUILDING_PEOPLE = "building_people"

class MainActivity : AppCompatActivity(), MapboxMap.OnMapClickListener {
    private var mapView: MapView? = null
    private var buildingGreenCollection: FeatureCollection? = null
    private var buildingYellowCollection: FeatureCollection? = null
    private var buildingRedCollection: FeatureCollection? = null
    private lateinit var lineOne: FeatureCollection
    private lateinit var lineTwo: FeatureCollection
    private var buildingMarkers: MapRepository = MapRepository.get()
    private var mapBoxMap: MapboxMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)


        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->

            mapBoxMap = mapboxMap
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments

                // init marker positions
                initBuildingsCollection()

//                // LINES
//                generateLineOne()
//                drawLines(lineOne, it)

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

                displayBuildingMarkerLayers(it)
                mapboxMap.addOnMapClickListener(this)
            }
        }
    }

    private fun displayBuildingMarkerLayers(style: Style) {

        // green
        style.addSource(GeoJsonSource(GREEN_BUILDINGS_SOURCE_ID, buildingGreenCollection))

        // Add the GreenBuildingLayer
        val greenCircleLayer: CircleLayer = CircleLayer(
            GREEN_CIRCLE_LAYER_ID,
            GREEN_BUILDINGS_SOURCE_ID
        )
            .withProperties(
                circleRadius(
                    interpolate(
                        linear(), zoom(),
                        stop(2, 20f),
                        stop(3, 15f)
                    )
                ),
                circleColor(parseColor("#3bb728"))
            )
        style.addLayer(greenCircleLayer)

        // yellow
        style.addSource(GeoJsonSource(YELLOW_BUILDINGS_SOURCE_ID, buildingYellowCollection))

        // Add the GreenBuildingLayer
        val yellowCircleLayer: CircleLayer = CircleLayer(
            YELLOW_CIRCLE_LAYER_ID,
            YELLOW_BUILDINGS_SOURCE_ID
        )
            .withProperties(
                circleRadius(
                    interpolate(
                        linear(), zoom(),
                        stop(2, 20f),
                        stop(3, 15f)
                    )
                ),
                circleColor(parseColor("#ffda3a"))
            )
        style.addLayer(yellowCircleLayer)

        // red
        style.addSource(GeoJsonSource(RED_BUILDINGS_SOURCE_ID, buildingRedCollection))

        // Add the GreenBuildingLayer
        val redCircleLayer: CircleLayer = CircleLayer(RED_CIRCLE_LAYER_ID, RED_BUILDINGS_SOURCE_ID)
            .withProperties(
                circleRadius(
                    interpolate(
                        linear(), zoom(),
                        stop(2, 20f),
                        stop(3, 15f)
                    )
                ),
                circleColor(parseColor("#da2f2f"))
            )
        style.addLayer(redCircleLayer)
    }

    private fun initBuildingsCollection() {
        val greenMarkerCoordinates: MutableList<Feature> = ArrayList()
        val yellowMarkerCoordinates: MutableList<Feature> = ArrayList()
        val redMarkerCoordinates: MutableList<Feature> = ArrayList()

        //building name

        buildingMarkers.buildingCoordinates.forEach { building ->
            val feature = Feature.fromGeometry(
                Point.fromLngLat(
                    building.longitude,
                    building.latitude
                )
            )
            feature.addStringProperty(BUILDING_NAME, building.buildingName)

            when (building.densityLevel) {
                3 -> redMarkerCoordinates.add(feature)
                2 -> yellowMarkerCoordinates.add(feature)
                else -> greenMarkerCoordinates.add(feature)
            }

            feature.addStringProperty(BUILDING_PEOPLE, building.people.toString())
            feature.addNumberProperty(BUILDING_FLOORS, building.floors)

        }

        buildingGreenCollection = FeatureCollection.fromFeatures(greenMarkerCoordinates)
        buildingYellowCollection = FeatureCollection.fromFeatures(yellowMarkerCoordinates)
        buildingRedCollection = FeatureCollection.fromFeatures(redMarkerCoordinates)
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
            GeoJsonSource(
                "line-source", featureCollection,
                GeoJsonOptions().withLineMetrics(true)
            )
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

    override fun onMapClick(point: LatLng): Boolean {
        if (mapBoxMap != null) {
            handleClickBuilding(mapBoxMap!!.getProjection().toScreenLocation(point))
        }
        return true
    }

    /**
     * This method handles click events for both layers.
     *
     *
     * The PROPERTY_SELECTED feature property is set to its opposite, so
     * that the visual toggling between circles and icons is correct.
     *
     * @param screenPoint the point on screen clicked
     */
    private fun handleClickBuilding(screenPoint: PointF): Boolean {
        val selectedGreenCircleFeatureList: List<Feature> =
            mapBoxMap?.queryRenderedFeatures(screenPoint, GREEN_CIRCLE_LAYER_ID) ?: listOf()
        val selectedYellowCircleFeatureList: List<Feature> =
            mapBoxMap?.queryRenderedFeatures(screenPoint, YELLOW_CIRCLE_LAYER_ID) ?: listOf()
        val selectedRedCircleFeatureList: List<Feature> =
            mapBoxMap?.queryRenderedFeatures(screenPoint, RED_CIRCLE_LAYER_ID) ?: listOf()
        if (selectedGreenCircleFeatureList.isNotEmpty()) {
            val selectedCircleFeature = selectedGreenCircleFeatureList[0]
            var buildingName = selectedCircleFeature.getStringProperty(BUILDING_NAME)
            var buildingPopulation =  selectedCircleFeature.getStringProperty(BUILDING_PEOPLE)
            var buildingFloors = selectedCircleFeature.getNumberProperty(BUILDING_FLOORS)
            Log.d("GreenDot", buildingName + buildingPopulation+ "$buildingFloors")

        }

        if (selectedYellowCircleFeatureList.isNotEmpty()) {
            val selectedCircleFeature = selectedYellowCircleFeatureList[0]
            var buildingName = selectedCircleFeature.getStringProperty(BUILDING_NAME)
            var buildingPopulation =  selectedCircleFeature.getStringProperty(BUILDING_PEOPLE)
            var buildingFloors = selectedCircleFeature.getNumberProperty(BUILDING_FLOORS)
            Log.d("YellowDot", buildingName + buildingPopulation+ "$buildingFloors")
        }

        if (selectedRedCircleFeatureList.isNotEmpty()) {
            val selectedCircleFeature = selectedRedCircleFeatureList[0]
            var buildingName = selectedCircleFeature.getStringProperty(BUILDING_NAME)
            var buildingPopulation =  selectedCircleFeature.getStringProperty(BUILDING_PEOPLE)
            var buildingFloors = selectedCircleFeature.getNumberProperty(BUILDING_FLOORS)
            Log.d("RedDot", buildingName + buildingPopulation+ "$buildingFloors")
        }
        return true
    }
}

