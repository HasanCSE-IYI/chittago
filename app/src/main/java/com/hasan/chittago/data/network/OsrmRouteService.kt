package com.hasan.chittago.data.network

import com.hasan.chittago.data.model.RouteResult
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class OsrmRouteService {

    suspend fun getRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        mode: String = "driving" // "driving", "bike", "walking"
    ): RouteResult? = withContext(Dispatchers.IO) {
        try {
            val osrmProfile = when (mode) {
                "walking" -> "foot"
                "bike" -> "bike"
                else -> "driving"
            }

            val urlString = "https://router.project-osrm.org/route/v1/$osrmProfile/" +
                    "$startLng,$startLat;$endLng,$endLat?overview=full&geometries=geojson&steps=false"

            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 12000
                setRequestProperty("User-Agent", "Chittago-App/1.0 (Android; Contact: HasanCSE-IYI)")
            }

            conn.connect()
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseText)
                val code = root.optString("code")

                if (code == "Ok") {
                    val routes = root.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val firstRoute = routes.getJSONObject(0)
                        val distanceMeters = firstRoute.getDouble("distance")
                        val durationSeconds = firstRoute.getDouble("duration")
                        val geometry = firstRoute.getJSONObject("geometry")
                        val coordinatesArray = geometry.getJSONArray("coordinates")

                        val distanceKm = distanceMeters / 1000.0
                        var durationMinutes = (durationSeconds / 60.0).toInt()
                        if (mode == "bike") {
                            durationMinutes = (durationMinutes * 0.85).toInt().coerceAtLeast(1)
                        }

                        return@withContext RouteResult(
                            distanceKm = distanceKm,
                            durationMinutes = durationMinutes.coerceAtLeast(1),
                            geojsonCoordinatesJson = coordinatesArray.toString(),
                            profile = mode
                        )
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
