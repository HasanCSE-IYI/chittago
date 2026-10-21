package com.hasan.chittago.data.network

import com.hasan.chittago.data.model.PlacePOI
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class PhotonSearchService {

    suspend fun searchOnline(query: String): List<PlacePOI> = withContext(Dispatchers.IO) {
        val results = mutableListOf<PlacePOI>()
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            // Strict Chattogram division bounding box
            val urlString = "https://photon.komoot.io/api/?q=$encodedQuery&bbox=90.4,20.5,92.9,24.5&limit=8"

            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Chittago-App/1.0 (Android; Contact: HasanCSE-IYI)")
            }

            conn.connect()
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseText)
                val features = root.optJSONArray("features") ?: return@withContext results

                for (i in 0 until features.length()) {
                    val feature = features.getJSONObject(i)
                    val geometry = feature.getJSONObject("geometry")
                    val coords = geometry.getJSONArray("coordinates")
                    val lng = coords.getDouble(0)
                    val lat = coords.getDouble(1)

                    val props = feature.getJSONObject("properties")
                    val name = props.optString("name", "Unknown Location")
                    val city = props.optString("city", props.optString("county", "Chattogram"))
                    val street = props.optString("street", "")
                    val category = props.optString("osm_value", "tourism")

                    results.add(
                        PlacePOI(
                            id = "photon_$i",
                            name = name,
                            nameBn = name,
                            category = if (category.contains("hospital")) "hospital" else "tourism",
                            district = city,
                            lat = lat,
                            lng = lng,
                            address = if (street.isNotEmpty()) "$street, $city" else city,
                            description = "OpenStreetMap Search Result"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        results
    }
}
