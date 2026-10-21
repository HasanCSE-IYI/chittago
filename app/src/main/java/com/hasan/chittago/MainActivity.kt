package com.hasan.chittago

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasan.chittago.data.cache.TileCacheManager
import com.hasan.chittago.data.model.PlacePOI
import com.hasan.chittago.data.network.OsrmRouteService
import com.hasan.chittago.data.network.PhotonSearchService
import com.hasan.chittago.data.repository.PoiRepository
import com.hasan.chittago.databinding.ActivityMainBinding
import com.hasan.chittago.ui.adapter.SearchSuggestionAdapter
import com.hasan.chittago.ui.dialog.DistrictSelectorDialog
import com.hasan.chittago.ui.view.MapViewBridge
import com.hasan.chittago.util.GeoUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var poiRepository: PoiRepository
    private lateinit var tileCacheManager: TileCacheManager
    private val osrmRouteService = OsrmRouteService()
    private val photonSearchService = PhotonSearchService()

    private lateinit var searchAdapter: SearchSuggestionAdapter
    private var searchJob: Job? = null

    // Default Fallback GPS: Chattogram Metro (GEC / Agrabad)
    private var currentUserLat: Double = 22.3569
    private var currentUserLng: Double = 91.8286
    private var hasRealGpsLocation: Boolean = false
    private var currentHeading: Float = 0f

    private var selectedPoi: PlacePOI? = null
    private var currentTravelMode = "driving" // "driving", "bike", "walking"
    private var currentLayerIndex = 0
    private val layerThemes = listOf("light", "dark", "osm")

    // Hardware Sensors
    private var locationManager: LocationManager? = null
    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            startLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        poiRepository = PoiRepository(this)
        tileCacheManager = TileCacheManager(this)

        setupWebView()
        setupSearch()
        setupCategoryChips()
        setupControls()
        setupSensors()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.mapWebView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            val bridge = MapViewBridge(
                onMarkerClickCallback = { id, name, lat, lng, category, district ->
                    runOnUiThread { handleMarkerClick(id, lat, lng) }
                },
                onMapMoveCallback = { lat, lng, zoom ->
                    // Optional tracking callback
                },
                onMapReadyCallback = {
                    runOnUiThread {
                        loadInitialMapData()
                    }
                }
            )
            addJavascriptInterface(bridge, "AndroidBridge")

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val url = request?.url ?: return super.shouldInterceptRequest(view, request)
                    if (tileCacheManager.isTileRequest(url)) {
                        val cachedResponse = tileCacheManager.getCachedResponse(url)
                        if (cachedResponse != null) return cachedResponse
                    }
                    return super.shouldInterceptRequest(view, request)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    loadInitialMapData()
                }
            }

            loadUrl("file:///android_asset/map/map.html")
        }
    }

    private fun loadInitialMapData() {
        val pois = poiRepository.getAllPois()
        renderPoisOnMap(pois)
        if (hasRealGpsLocation) {
            updateJsUserLocation()
        }
    }

    private fun renderPoisOnMap(pois: List<PlacePOI>) {
        val jsonArray = JSONArray()
        for (poi in pois) {
            val obj = JSONObject().apply {
                put("id", poi.id)
                put("name", poi.name)
                put("nameBn", poi.nameBn)
                put("category", poi.category)
                put("district", poi.district)
                put("lat", poi.lat)
                put("lng", poi.lng)
            }
            jsonArray.put(obj)
        }
        val jsCommand = "renderPois('${jsonArray.toString().replace("'", "\\'")}')"
        binding.mapWebView.evaluateJavascript(jsCommand, null)
    }

    private fun setupSearch() {
        searchAdapter = SearchSuggestionAdapter(emptyList()) { poi ->
            onPoiSelected(poi)
        }
        binding.rvSuggestions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = searchAdapter
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE

                if (query.isEmpty()) {
                    binding.cardSuggestions.visibility = View.GONE
                    return
                }

                // Instant Local Search
                val localMatches = poiRepository.searchPois(query)
                if (localMatches.isNotEmpty()) {
                    binding.cardSuggestions.visibility = View.VISIBLE
                    searchAdapter.updateList(localMatches)
                }

                // Debounced Online Search via Photon for comprehensive coverage
                searchJob?.cancel()
                if (query.length >= 3) {
                    searchJob = lifecycleScope.launch {
                        delay(350)
                        val onlineResults = photonSearchService.searchOnline(query)
                        val combined = (localMatches + onlineResults).distinctBy { "${it.lat},${it.lng}" }
                        if (combined.isNotEmpty()) {
                            binding.cardSuggestions.visibility = View.VISIBLE
                            searchAdapter.updateList(combined)
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
            binding.cardSuggestions.visibility = View.GONE
        }
    }

    private fun onPoiSelected(poi: PlacePOI) {
        selectedPoi = poi
        binding.cardSuggestions.visibility = View.GONE
        binding.etSearch.clearFocus()

        // Fly map camera to selected POI
        val jsCommand = "flyToLocation(${poi.lat}, ${poi.lng}, 15)"
        binding.mapWebView.evaluateJavascript(jsCommand, null)

        showPoiDetailSheet(poi)
    }

    private fun showPoiDetailSheet(poi: PlacePOI) {
        selectedPoi = poi
        binding.cardPoiDetail.visibility = View.VISIBLE
        binding.tvPoiName.text = poi.nameBn.ifEmpty { poi.name }
        binding.tvPoiDistrict.text = poi.district
        binding.tvPoiDescription.text = poi.description.ifEmpty { poi.address }

        val distanceKm = GeoUtils.calculateDistanceKm(currentUserLat, currentUserLng, poi.lat, poi.lng)
        binding.tvPoiDistance.text = "দূরত্ব: ${GeoUtils.formatDistance(distanceKm)}"

        // Emergency / Phone call button
        if (poi.phone.isNotEmpty()) {
            binding.btnCallPoi.visibility = View.VISIBLE
            binding.btnCallPoi.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${poi.phone}"))
                    startActivity(intent)
                } catch (_: Exception) {}
            }
        } else {
            binding.btnCallPoi.visibility = View.GONE
        }

        binding.btnGetDirections.setOnClickListener {
            startNavigationTo(poi)
        }

        binding.btnClosePoiDetail.setOnClickListener {
            binding.cardPoiDetail.visibility = View.GONE
        }
    }

    private fun handleMarkerClick(id: String, lat: Double, lng: Double) {
        val poi = poiRepository.getPoiById(id) ?: PlacePOI(
            id = id,
            name = "Selected Spot",
            nameBn = "নির্বাচিত স্থান",
            category = "tourism",
            district = "Chattogram",
            lat = lat,
            lng = lng
        )
        showPoiDetailSheet(poi)
    }

    private fun startNavigationTo(poi: PlacePOI) {
        binding.cardPoiDetail.visibility = View.GONE
        binding.cardRouteInfo.visibility = View.VISIBLE
        binding.tvRouteDestination.text = "গন্তব্য: ${poi.nameBn.ifEmpty { poi.name }}"

        requestRoute(poi.lat, poi.lng, currentTravelMode)
    }

    private fun requestRoute(destLat: Double, destLng: Double, mode: String) {
        lifecycleScope.launch {
            val route = osrmRouteService.getRoute(
                startLat = currentUserLat,
                startLng = currentUserLng,
                endLat = destLat,
                endLng = destLng,
                mode = mode
            )

            if (route != null) {
                binding.tvRouteDistance.text = "(${GeoUtils.formatDistance(route.distanceKm)})"
                binding.tvRouteDuration.text = GeoUtils.formatDuration(route.durationMinutes)

                val routeColor = when (mode) {
                    "walking" -> "#10b981"
                    "bike" -> "#f59e0b"
                    else -> "#0ea5e9"
                }

                val jsCommand = "drawRoute('${route.geojsonCoordinatesJson}', '$routeColor')"
                binding.mapWebView.evaluateJavascript(jsCommand, null)
            } else {
                Toast.makeText(this@MainActivity, "রুট পাওয়া যায়নি বা অফলাইন মোডে আছেন", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCategoryChips() {
        val chips = listOf(
            binding.chipAll to "all",
            binding.chipTourism to "tourism",
            binding.chipHospital to "hospital",
            binding.chipMezban to "mezban",
            binding.chipTransport to "transport",
            binding.chipEmergency to "emergency"
        )

        for ((chipView, category) in chips) {
            chipView.setOnClickListener {
                // Update styling
                for ((c, _) in chips) {
                    if (c == chipView) {
                        c.setBackgroundResource(R.drawable.bg_chip_selected)
                        c.setTextColor(ContextCompat.getColor(this, R.color.surface_white))
                    } else {
                        c.setBackgroundResource(R.drawable.bg_chip_unselected)
                        c.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                    }
                }
                val filtered = poiRepository.getPoisByCategory(category)
                renderPoisOnMap(filtered)
            }
        }
    }

    private fun setupControls() {
        // District selection
        val openDistrictSelector = {
            DistrictSelectorDialog { district ->
                binding.mapWebView.evaluateJavascript("flyToLocation(${district.lat}, ${district.lng}, 13)", null)
                Toast.makeText(this, "${district.nameBn} জেলায় যাচ্ছেন", Toast.LENGTH_SHORT).show()
            }.show(supportFragmentManager, DistrictSelectorDialog.TAG)
        }
        binding.fabDistrictQuick.setOnClickListener { openDistrictSelector() }
        binding.btnDistrictFilter.setOnClickListener { openDistrictSelector() }

        // Layer switcher (Day / Night / OSM)
        binding.fabLayers.setOnClickListener {
            currentLayerIndex = (currentLayerIndex + 1) % layerThemes.size
            val nextTheme = layerThemes[currentLayerIndex]
            binding.mapWebView.evaluateJavascript("setTheme('$nextTheme')", null)
            val themeName = when (nextTheme) {
                "dark" -> "নাইট মোড (Dark)"
                "osm" -> "ওপেনস্ট্রিটম্যাপ (Standard)"
                else -> "ডে মোড (Clean Light)"
            }
            Toast.makeText(this, themeName, Toast.LENGTH_SHORT).show()
        }

        // My Location
        binding.fabMyLocation.setOnClickListener {
            if (!hasRealGpsLocation) {
                checkLocationPermissions()
            }
            binding.mapWebView.evaluateJavascript("flyToLocation($currentUserLat, $currentUserLng, 16)", null)
        }

        // Navigation Mode Selectors
        binding.btnModeDriving.setOnClickListener {
            updateModeSelector("driving")
        }
        binding.btnModeBike.setOnClickListener {
            updateModeSelector("bike")
        }
        binding.btnModeWalking.setOnClickListener {
            updateModeSelector("walking")
        }

        // Close route
        binding.btnCloseRoute.setOnClickListener {
            binding.cardRouteInfo.visibility = View.GONE
            binding.mapWebView.evaluateJavascript("clearRoute()", null)
        }
    }

    private fun updateModeSelector(mode: String) {
        currentTravelMode = mode
        val modes = listOf(
            binding.btnModeDriving to "driving",
            binding.btnModeBike to "bike",
            binding.btnModeWalking to "walking"
        )
        for ((btn, m) in modes) {
            if (m == mode) {
                btn.setBackgroundResource(R.drawable.bg_chip_selected)
                btn.setTextColor(ContextCompat.getColor(this, R.color.surface_white))
            } else {
                btn.setBackgroundResource(R.drawable.bg_chip_unselected)
                btn.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            }
        }
        selectedPoi?.let {
            requestRoute(it.lat, it.lng, currentTravelMode)
        }
    }

    private fun setupSensors() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        checkLocationPermissions()
    }

    private fun checkLocationPermissions() {
        val fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            startLocationUpdates()
        } else {
            locationPermissionRequest.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            locationManager?.let { lm ->
                val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (isGpsEnabled) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000L, 5f, this)
                }
                if (isNetworkEnabled) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 5f, this)
                }

                val lastKnown = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                lastKnown?.let { onLocationChanged(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        currentUserLat = location.latitude
        currentUserLng = location.longitude
        hasRealGpsLocation = true
        updateJsUserLocation(location.accuracy)
    }

    private fun updateJsUserLocation(accuracy: Float = 15f) {
        val jsCommand = "updateUserLocation($currentUserLat, $currentUserLng, $currentHeading, $accuracy)"
        binding.mapWebView.evaluateJavascript(jsCommand, null)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            currentHeading = (azimuthDeg + 360) % 360
            updateJsUserLocation()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        rotationSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(this)
    }
}
