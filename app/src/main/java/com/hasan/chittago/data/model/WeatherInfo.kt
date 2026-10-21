package com.hasan.chittago.data.model

data class WeatherInfo(
    val districtName: String,
    val temperatureC: Double,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val precipitationMm: Double,
    val weatherCode: Int,
    val conditionBn: String,
    val advisoryLevel: String, // "safe", "moderate", "danger"
    val advisoryTitleBn: String,
    val advisoryMessageBn: String,
    val dailyMaxTemp: Double = 0.0,
    val dailyMinTemp: Double = 0.0,
    val rainProbability: Int = 0
)
