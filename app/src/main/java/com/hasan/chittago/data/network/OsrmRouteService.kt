package com.hasan.chittago.data.network

import com.hasan.chittago.data.model.RouteResult
import com.hasan.chittago.data.model.RouteStep
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
                    "$startLng,$startLat;$endLng,$endLat?overview=full&geometries=geojson&steps=true"

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

                        val parsedSteps = mutableListOf<RouteStep>()
                        val legs = firstRoute.optJSONArray("legs")
                        if (legs != null && legs.length() > 0) {
                            val firstLeg = legs.getJSONObject(0)
                            val stepsArray = firstLeg.optJSONArray("steps")
                            if (stepsArray != null) {
                                for (i in 0 until stepsArray.length()) {
                                    val stepObj = stepsArray.getJSONObject(i)
                                    val stepDist = stepObj.optDouble("distance", 0.0)
                                    val stepDuration = stepObj.optDouble("duration", 0.0)
                                    val streetName = stepObj.optString("name", "")
                                    val maneuver = stepObj.optJSONObject("maneuver")
                                    val mType = maneuver?.optString("type", "") ?: ""
                                    val mModifier = maneuver?.optString("modifier", "") ?: ""

                                    val instructionBn = formatBengaliManeuver(mType, mModifier, streetName)
                                    parsedSteps.add(
                                        RouteStep(
                                            instruction = instructionBn,
                                            distanceMeters = stepDist,
                                            durationSeconds = stepDuration,
                                            type = mType,
                                            modifier = mModifier,
                                            name = streetName
                                        )
                                    )
                                }
                            }
                        }

                        val distanceKm = distanceMeters / 1000.0
                        var durationMinutes = (durationSeconds / 60.0).toInt()
                        if (mode == "bike") {
                            durationMinutes = (durationMinutes * 0.85).toInt().coerceAtLeast(1)
                        }

                        return@withContext RouteResult(
                            distanceKm = distanceKm,
                            durationMinutes = durationMinutes.coerceAtLeast(1),
                            geojsonCoordinatesJson = coordinatesArray.toString(),
                            profile = mode,
                            steps = parsedSteps
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

    private fun formatBengaliManeuver(type: String, modifier: String, streetName: String): String {
        val street = if (streetName.isNotBlank()) " $streetName এ" else ""
        return when (type) {
            "depart" -> if (streetName.isNotBlank()) "$streetName ধরে যাত্রা শুরু করুন" else "যাত্রা শুরু করুন"
            "arrive" -> "গন্তব্যে পৌঁছে গেছেন"
            "roundabout", "rotary" -> "গোলচত্বর দিয়ে সোজা চলুন"
            "turn" -> when (modifier) {
                "left", "sharp left", "slight left" -> "বামে মোড় নিন$street"
                "right", "sharp right", "slight right" -> "ডানে মোড় নিন$street"
                "uturn" -> "ইউ-টার্ন নিন$street"
                else -> "মোড় নিন$street"
            }
            "end of road" -> when (modifier) {
                "left" -> "রাস্তার শেষে বামে যান"
                "right" -> "রাস্তার শেষে ডানে যান"
                else -> "রাস্তার শেষ পর্যন্ত চলুন"
            }
            "fork" -> if (modifier.contains("left")) "বামের রাস্তা বেছে নিন" else "ডানের রাস্তা বেছে নিন"
            else -> if (streetName.isNotBlank()) "$streetName ধরে সোজা চলুন" else "সোজা এগিয়ে চলুন"
        }
    }
}
