package com.hasan.chittago.data.model

data class RouteResult(
    val distanceKm: Double,
    val durationMinutes: Int,
    val geojsonCoordinatesJson: String,
    val startAddress: String = "",
    val endAddress: String = "",
    val profile: String = "driving"
)
