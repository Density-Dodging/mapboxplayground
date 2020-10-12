package com.bignerdranch.android.mapboxplayground

// geo json

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Color.parseColor
import android.graphics.PointF
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.lang.ref.WeakReference


private const val GREEN_CIRCLE_LAYER_ID = "green_buildings"
private const val GREEN_BUILDINGS_SOURCE_ID = "green_buildings_source"

private const val YELLOW_CIRCLE_LAYER_ID = "yellow_buildings"
private const val YELLOW_BUILDINGS_SOURCE_ID = "yellow_buildings_source"

private const val RED_CIRCLE_LAYER_ID = "red_buildings"
private const val RED_BUILDINGS_SOURCE_ID = "red_buildings_source"

private const val FAST_LINE_SOURCE = "fast_line_source"
private const val SAFE_LINE_SOURCE = "safe_line_source"

private const val FAST_LINE_LAYER_ID = "fast_line"
private const val SAFE_LINE_LAYER_ID = "safe_line"

private const val SAFE_LINE_COLOR = "#5e96e5"
private const val FAST_LINE_COLOR = "#eb34d5"

const val BUILDING_ID = "building_id"

const val REQUEST_CODE_BUILDING_INFO = 0

class MainActivity : AppCompatActivity(), MapboxMap.OnMapClickListener, PermissionsListener {
    private lateinit var mapView: MapView
    private var buildingGreenCollection: FeatureCollection? = null
    private var buildingYellowCollection: FeatureCollection? = null
    private var buildingRedCollection: FeatureCollection? = null
    private lateinit var lineOne: FeatureCollection
    private lateinit var lineTwo: FeatureCollection
    private var buildingMarkers: MapRepository = MapRepository.get()
    private lateinit var mapBoxMap: MapboxMap

    private lateinit var searchBar: CustomAutoCompleteTextView
    private lateinit var suggestionsList: ListView

    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

    private lateinit var locationEngine: LocationEngine
    private val callback: LocationChangeListeningActivityLocationCallback =
        LocationChangeListeningActivityLocationCallback(this)

    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    private var currentCoordinates: MutableList<Double>? = null

    private var buildingTo: Building? = null

    private lateinit var mapStyle: Style

    //////////////////////// LIFECYCLE ////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)

        initializeUiElements()
        initializeMap(savedInstanceState)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_CODE_BUILDING_INFO) {
            val id = data?.getStringExtra(DIRECTIONS_REQUESTED_FOR_ID).toString()
            var building = buildingMarkers.getBuildingFromId(id)

            if (building != null) {
                buildingTo = building
                handleDisplayingDirections()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()

        // if moving from background to current initialize location tracking again
        if (this::locationEngine.isInitialized) {
            initLocationEngine()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()

        // stop tracking in background
        locationEngine.removeLocationUpdates(callback)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    //////////////////////// OTHER FCNS ////////////////////////

    private fun initializeUiElements() {
        mapView = findViewById(R.id.mapView)
        searchBar = findViewById(R.id.buildingSearch)

        searchBar.setOnClickListener {

        }

        buildingMarkers.buildings.observe(this) {
            val adapter =
                BuildingAdapter(this, android.R.layout.simple_list_item_1, it)

            searchBar.setAdapter(adapter)
        }

        searchBar.setOnItemClickListener { parent, _, position, id ->
            buildingTo = parent.adapter.getItem(position) as Building?
            searchBar.setText(buildingTo?.buildingName)
            handleDisplayingDirections()
            hideSoftKeyboard(this)
        }
    }

    private fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager: InputMethodManager = activity.getSystemService(
            INPUT_METHOD_SERVICE
        ) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            activity.currentFocus!!.windowToken, 0
        )
    }

    private fun handleDisplayingDirections() {
        // make sure we have user's current position
        // make sure user selected a building prior
        if (buildingTo != null) {
            if (currentCoordinates != null) {
                // make back-end request (user's location - building id)
                buildingMarkers.loadRoute(
                    currentCoordinates!![0],
                    currentCoordinates!![1],
                    buildingTo!!.id,
                    false
                )
                buildingMarkers.loadRoute(
                    currentCoordinates!![0],
                    currentCoordinates!![1],
                    buildingTo!!.id,
                    true
                )

                // then, the paths will be drawn through observer
            } else {
                // didn't permit to use device's location
                Toast.makeText(
                    this,
                    "Please, make sure to allow the app to use your current location",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }

    private fun initializeMap(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->

            mapBoxMap = mapboxMap
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                mapStyle = it
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                enableLocationComponent()

                // 3D - BUILDINGS
                displayBuildings3D(it)

                // markers
                buildingMarkers.buildings.observe(this) { buildings ->
                    // init marker positions
                    initBuildingsCollection(buildings)
                    displayBuildingMarkerLayers(it)
                }

                // routes
                buildingMarkers.pathFastest.observe(this) { fastestPath ->
                    displayRoute(fastestPath, false)
                }
                buildingMarkers.pathSafest.observe(this) { safestPath ->
                    displayRoute(safestPath, true)
                }

                mapboxMap.addOnMapClickListener(this)
            }
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        handleClickBuilding(mapBoxMap.projection.toScreenLocation(point))
        return true
    }

    private fun enableLocationComponent() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this) && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true).accuracyColor(R.color.colorPrimary)
                .build()

            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, mapStyle)
                    .locationComponentOptions(customLocationComponentOptions)
                    .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapBoxMap.locationComponent.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }

            initLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG)
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent()
            handleDisplayingDirections()
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG)
                .show()
            finish()
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        val request: LocationEngineRequest = LocationEngineRequest.Builder(
            DEFAULT_INTERVAL_IN_MILLISECONDS * 10
        )
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()
        locationEngine.requestLocationUpdates(request, callback, mainLooper)
        locationEngine.getLastLocation(callback)
    }

    private fun displayBuildings3D(style: Style) {
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
        style.addLayer(fillExtrusionLayer)
    }

    private fun displayRoute(path: List<List<Double>>, safe: Boolean) {
        var routeCoordinates = ArrayList<Point>()

        var sourceId: String = FAST_LINE_SOURCE
        var layerId: String = FAST_LINE_LAYER_ID
        var color: String = FAST_LINE_COLOR

        if (safe) {
            sourceId = SAFE_LINE_SOURCE
            layerId = SAFE_LINE_LAYER_ID
            color = SAFE_LINE_COLOR
        }

        path.forEach { coordinate ->
            routeCoordinates.add(Point.fromLngLat(coordinate[1], coordinate[0]))
        }

        mapStyle.addSource(
            GeoJsonSource(
                sourceId,
                FeatureCollection.fromFeatures(
                    arrayOf(
                        Feature.fromGeometry(
                            LineString.fromLngLats(routeCoordinates)
                        )
                    )
                )
            )
        )

        mapStyle.addLayer(
            LineLayer(layerId, sourceId).withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(6f),
                lineOpacity(1f),
                lineColor(parseColor(color))
            )
        )
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

    private fun initBuildingsCollection(buildings: List<Building>) {
        val greenMarkerCoordinates: MutableList<Feature> = ArrayList()
        val yellowMarkerCoordinates: MutableList<Feature> = ArrayList()
        val redMarkerCoordinates: MutableList<Feature> = ArrayList()

        //building name

        buildings.forEach { building ->
            val feature = Feature.fromGeometry(
                Point.fromLngLat(
                    building.longitude,
                    building.latitude
                )
            )

            feature.addStringProperty(BUILDING_ID, building.id)

            when (building.densityLevel) {
                3 -> redMarkerCoordinates.add(feature)
                2 -> yellowMarkerCoordinates.add(feature)
                else -> greenMarkerCoordinates.add(feature)
            }
        }

        buildingGreenCollection = FeatureCollection.fromFeatures(greenMarkerCoordinates)
        buildingYellowCollection = FeatureCollection.fromFeatures(yellowMarkerCoordinates)
        buildingRedCollection = FeatureCollection.fromFeatures(redMarkerCoordinates)
    }


    /**
     * This method handles click events for all the layers.
     *
     *
     * The new building detail activity is started with properties obtained from
     * the circle that was clicked on
     *
     * @param screenPoint the point on screen clicked
     */
    private fun handleClickBuilding(screenPoint: PointF): Boolean {

        var buildingId: String = ""

        val selectedGreenCircleFeatureList: List<Feature> =
            mapBoxMap.queryRenderedFeatures(screenPoint, GREEN_CIRCLE_LAYER_ID) ?: listOf()
        val selectedYellowCircleFeatureList: List<Feature> =
            mapBoxMap.queryRenderedFeatures(screenPoint, YELLOW_CIRCLE_LAYER_ID) ?: listOf()
        val selectedRedCircleFeatureList: List<Feature> =
            mapBoxMap.queryRenderedFeatures(screenPoint, RED_CIRCLE_LAYER_ID) ?: listOf()
        if (selectedGreenCircleFeatureList.isNotEmpty()) {
            val selectedCircleFeature = selectedGreenCircleFeatureList[0]
            buildingId = selectedCircleFeature.getStringProperty(BUILDING_ID)
        }

        if (selectedYellowCircleFeatureList.isNotEmpty()) {
            val selectedCircleFeature = selectedYellowCircleFeatureList[0]
            buildingId = selectedCircleFeature.getStringProperty(BUILDING_ID)
        }

        if (selectedRedCircleFeatureList.isNotEmpty()) {
            val selectedCircleFeature = selectedRedCircleFeatureList[0]
            buildingId = selectedCircleFeature.getStringProperty(BUILDING_ID)
        }

        startBuildingDetailsIntent(buildingId)
        return true
    }

    private fun startBuildingDetailsIntent(buildingId: String) {
        // check to make sure the fields for initialized
        if (buildingId.isNotEmpty()) {
            val intent = BuildingDetailsActivity.newIntent(this@MainActivity, buildingId)
            startActivityForResult(intent, REQUEST_CODE_BUILDING_INFO)
        }
    }

    ////////////////// INNER CLASSES //////////////////
    inner class LocationChangeListeningActivityLocationCallback internal constructor(activity: MainActivity?) :
        LocationEngineCallback<LocationEngineResult?> {
        private val activityWeakReference: WeakReference<MainActivity> = WeakReference(activity)

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        override fun onSuccess(result: LocationEngineResult?) {
            val activity: MainActivity? = activityWeakReference.get()
            if (activity != null) {
                val location: Location = result?.lastLocation ?: return

                // Create a Toast which displays the new location's coordinates
                if (activity != null && result.lastLocation != null) {

                    currentCoordinates = mutableListOf(location.latitude, location.longitude)
                }

                // Pass the new location to the Maps SDK's LocationComponent
                if (result.lastLocation != null) {
                    activity.mapBoxMap.locationComponent
                        .forceLocationUpdate(result.lastLocation)
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can't be captured
         *
         * @param exception the exception message
         */
        override fun onFailure(exception: Exception) {
            val activity: MainActivity? = activityWeakReference.get()
            if (activity != null) {
                Toast.makeText(
                    activity, exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    inner class BuildingAdapter(
        context: Context,
        private val layoutResource: Int,
        private val allBuildings: List<Building>
    ) : ArrayAdapter<Building>(context, layoutResource, allBuildings), Filterable {
        private var filteredBuildings: List<Building> = allBuildings

        override fun getCount(): Int {
            return filteredBuildings.size
        }

        override fun getItem(p0: Int): Building? {
            return filteredBuildings[p0]
        }

        override fun getItemId(p0: Int): Long {
            // Or just return p0

//            val latStr = filteredBuildings[p0].latitude.toString()
//            var lonStr = filteredBuildings[p0].longitude.toString()
//
//            var uniqueId = latStr
//                .substring(4, latStr.length) + lonStr.substring(4, lonStr.length)

            return p0.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: TextView = convertView as TextView? ?: LayoutInflater.from(context)
                .inflate(layoutResource, parent, false) as TextView
            view.text =
                "${filteredBuildings[position].buildingName} | ${filteredBuildings[position].id}"
            return view
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun publishResults(
                    charSequence: CharSequence?,
                    filterResults: Filter.FilterResults
                ) {
                    filteredBuildings = filterResults.values as List<Building>
                    notifyDataSetChanged()
                }

                override fun performFiltering(charSequence: CharSequence?): FilterResults {
                    val queryString = charSequence?.toString()?.toLowerCase()

                    val filterResults = FilterResults()
                    filterResults.values = when (queryString == null || queryString.isEmpty()) {
                        true -> allBuildings
                        else -> allBuildings.filter {
                            it.buildingName.toLowerCase().contains(queryString) ||
                                    it.id.toLowerCase().contains(queryString)
                        }
                    }
                    return filterResults
                }
            }
        }
    }
}

