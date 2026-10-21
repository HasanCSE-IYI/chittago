package com.hasan.chittago.data.model

data class RouteStep(
    val instruction: String,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val type: String = "",
    val modifier: String = "",
    val name: String = ""
)

data class RouteResult(
    val distanceKm: Double,
    val durationMinutes: Int,
    val geojsonCoordinatesJson: String,
    val startAddress: String = "",
    val endAddress: String = "",
    val profile: String = "driving",
    val steps: List<RouteStep> = emptyList()
)
