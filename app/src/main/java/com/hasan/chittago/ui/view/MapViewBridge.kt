package com.hasan.chittago.ui.view

import android.webkit.JavascriptInterface

class MapViewBridge(
    private val onMarkerClickCallback: (id: String, name: String, lat: Double, lng: Double, category: String, district: String) -> Unit,
    private val onMapMoveCallback: (lat: Double, lng: Double, zoom: Int) -> Unit,
    private val onMapReadyCallback: () -> Unit
) {

    @JavascriptInterface
    fun onMarkerClicked(id: String, name: String, lat: Double, lng: Double, category: String, district: String) {
        onMarkerClickCallback(id, name, lat, lng, category, district)
    }

    @JavascriptInterface
    fun onMapMoved(lat: Double, lng: Double, zoom: Int) {
        onMapMoveCallback(lat, lng, zoom)
    }

    @JavascriptInterface
    fun onMapReady() {
        onMapReadyCallback()
    }
}
