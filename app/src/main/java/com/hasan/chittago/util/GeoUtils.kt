package com.hasan.chittago.util

import java.util.Locale
import kotlin.math.*

object GeoUtils {
    // Chattogram Division Geographic Bounding Box
    const val CTG_MIN_LAT = 20.50
    const val CTG_MAX_LAT = 24.50
    const val CTG_MIN_LNG = 90.40
    const val CTG_MAX_LNG = 92.90

    fun isInsideChattogramDivision(lat: Double, lng: Double): Boolean {
        return lat in CTG_MIN_LAT..CTG_MAX_LAT && lng in CTG_MIN_LNG..CTG_MAX_LNG
    }

    /**
     * Calculates distance between two coordinates in kilometers using Haversine formula
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun formatDistance(km: Double): String {
        return if (km < 1.0) {
            "${(km * 1000).roundToInt()} মি."
        } else {
            String.format(Locale.US, "%.1f কিমি", km)
        }
    }

    fun formatDuration(minutes: Int): String {
        return if (minutes < 60) {
            "$minutes মিনিট"
        } else {
            val hours = minutes / 60
            val remMinutes = minutes % 60
            if (remMinutes > 0) "$hours ঘণ্টা $remMinutes মিনিট" else "$hours ঘণ্টা"
        }
    }
}
