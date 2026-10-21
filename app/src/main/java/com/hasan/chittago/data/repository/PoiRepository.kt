package com.hasan.chittago.data.repository

import android.content.Context
import com.hasan.chittago.data.model.PlacePOI
import org.json.JSONArray

class PoiRepository(private val context: Context) {

    private val cachedPois = mutableListOf<PlacePOI>()

    init {
        loadPoisFromAssets()
    }

    private fun loadPoisFromAssets() {
        try {
            val jsonString = context.assets.open("data/chattogram_pois.json")
                .bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            cachedPois.clear()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                cachedPois.add(
                    PlacePOI(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        nameBn = obj.optString("nameBn", obj.getString("name")),
                        category = obj.getString("category"),
                        district = obj.getString("district"),
                        lat = obj.getDouble("lat"),
                        lng = obj.getDouble("lng"),
                        address = obj.optString("address", ""),
                        phone = obj.optString("phone", ""),
                        description = obj.optString("description", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllPois(): List<PlacePOI> = cachedPois

    fun getPoisByCategory(category: String): List<PlacePOI> {
        if (category == "all") return cachedPois
        return cachedPois.filter { it.category.equals(category, ignoreCase = true) }
    }

    fun getPoisByDistrict(district: String): List<PlacePOI> {
        if (district.equals("all", ignoreCase = true)) return cachedPois
        return cachedPois.filter { it.district.equals(district, ignoreCase = true) }
    }

    fun searchPois(query: String): List<PlacePOI> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return cachedPois
        return cachedPois.filter { poi ->
            poi.name.lowercase().contains(q) ||
            poi.nameBn.lowercase().contains(q) ||
            poi.district.lowercase().contains(q) ||
            poi.description.lowercase().contains(q) ||
            poi.address.lowercase().contains(q)
        }
    }

    fun getPoiById(id: String): PlacePOI? {
        return cachedPois.firstOrNull { it.id == id }
    }
}
