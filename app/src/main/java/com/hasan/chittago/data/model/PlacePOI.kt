package com.hasan.chittago.data.model

data class PlacePOI(
    val id: String,
    val name: String,
    val nameBn: String,
    val category: String,
    val district: String,
    val lat: Double,
    val lng: Double,
    val address: String = "",
    val phone: String = "",
    val description: String = ""
)
