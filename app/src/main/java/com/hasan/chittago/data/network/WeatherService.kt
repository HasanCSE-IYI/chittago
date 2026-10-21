package com.hasan.chittago.data.network

import com.hasan.chittago.data.model.WeatherInfo
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class WeatherService {

    suspend fun getDistrictWeather(
        lat: Double,
        lng: Double,
        districtName: String
    ): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            val urlString = String.format(
                Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,precipitation&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=Asia%%2FDhaka",
                lat,
                lng
            )

            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 10000
                setRequestProperty("User-Agent", "Chittago-App/1.0 (Android; Contact: HasanCSE-IYI)")
            }

            conn.connect()
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseText)
                val current = root.optJSONObject("current") ?: return@withContext null
                val daily = root.optJSONObject("daily")

                val temp = current.optDouble("temperature_2m", 28.0)
                val humidity = current.optInt("relative_humidity_2m", 65)
                val weatherCode = current.optInt("weather_code", 0)
                val windSpeed = current.optDouble("wind_speed_10m", 5.0)
                val precip = current.optDouble("precipitation", 0.0)

                var maxTemp = temp + 3.0
                var minTemp = temp - 4.0
                var rainProb = 10

                if (daily != null) {
                    val maxArr = daily.optJSONArray("temperature_2m_max")
                    val minArr = daily.optJSONArray("temperature_2m_min")
                    val probArr = daily.optJSONArray("precipitation_probability_max")
                    if (maxArr != null && maxArr.length() > 0) maxTemp = maxArr.optDouble(0, maxTemp)
                    if (minArr != null && minArr.length() > 0) minTemp = minArr.optDouble(0, minTemp)
                    if (probArr != null && probArr.length() > 0) rainProb = probArr.optInt(0, rainProb)
                }

                val conditionBn = mapWeatherCodeToBengali(weatherCode)
                val (level, title, message) = evaluateHillAdvisory(weatherCode, precip, windSpeed, districtName)

                return@withContext WeatherInfo(
                    districtName = districtName,
                    temperatureC = temp,
                    humidityPercent = humidity,
                    windSpeedKmh = windSpeed,
                    precipitationMm = precip,
                    weatherCode = weatherCode,
                    conditionBn = conditionBn,
                    advisoryLevel = level,
                    advisoryTitleBn = title,
                    advisoryMessageBn = message,
                    dailyMaxTemp = maxTemp,
                    dailyMinTemp = minTemp,
                    rainProbability = rainProb
                )
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun mapWeatherCodeToBengali(code: Int): String {
        return when (code) {
            0 -> "রৌদ্রোজ্জ্বল (Clear Sky)"
            1, 2 -> "আংশিক মেঘলা (Partly Cloudy)"
            3 -> "মেঘলা আকাশ (Overcast)"
            45, 48 -> "কুয়াশাচ্ছন্ন (Foggy)"
            51, 53, 55 -> "ঝিরিঝিরি বৃষ্টি (Drizzle)"
            61, 63 -> "মাঝারি বৃষ্টিপাত (Rain)"
            65, 82 -> "ভারী বর্ষণ (Heavy Rain)"
            80, 81 -> "বৃষ্টির সম্ভাবনা (Showers)"
            95, 96, 99 -> "বজ্রঝড় / কালবৈশাখী (Thunderstorm)"
            else -> "স্বাভাবিক আবহাওয়া (Fair)"
        }
    }

    private fun evaluateHillAdvisory(
        code: Int,
        precipMm: Double,
        windSpeed: Double,
        district: String
    ): Triple<String, String, String> {
        val isHilly = district in listOf("Bandarban", "Rangamati", "Khagrachhari", "বান্দরবান", "রাঙ্গামাটি", "খাগড়াছড়ি")
        val isCoastal = district in listOf("Cox's Bazar", "Chattogram", "কক্সবাজার", "চট্টগ্রাম")

        return when {
            code in listOf(65, 82, 95, 96, 99) || precipMm >= 25.0 -> {
                Triple(
                    "danger",
                    "⚠️ পাহাড় ধস ও পাহাড়ি ঢলের সতর্কতা",
                    if (isHilly) {
                        "অতিভারী বর্ষণের কারণে পাহাড় ধসের প্রবল ঝুঁকি রয়েছে। সাজেক ও বান্দরবানের পাহাড়ি ট্রেইল এবং ঝর্ণার গভীর খাদ পরিহার করুন।"
                    } else if (isCoastal) {
                        "সাগরে উত্তাল ঢেউ ও জলোচ্ছ্বাসের আশঙ্কা রয়েছে। পর্যটকদের সমুদ্রে নামা থেকে বিরত থাকার পরামর্শ দেওয়া হচ্ছে।"
                    } else {
                        "ভারী বৃষ্টি ও বজ্রপাতের কারণে জরুরি প্রয়োজন ছাড়া ঘরের বাইরে যাওয়া থেকে সতর্ক থাকুন।"
                    }
                )
            }
            code in listOf(51, 53, 55, 61, 63, 80, 81) || precipMm > 2.0 || windSpeed > 30.0 -> {
                Triple(
                    "moderate",
                    "⚡ পাহাড়ি পথে চলাচলে সতর্কতা",
                    if (isHilly) {
                        "বৃষ্টির কারণে পাহাড়ি ও ঢালু রাস্তা পিচ্ছিল হতে পারে। চাদের গাড়ি ও মোটরসাইকেল সাবধানে চালান।"
                    } else {
                        "আংশিক বৃষ্টিপাত। চলাচলের সময় ছাতা ও প্রয়োজনীয় রেইনকোট সাথে রাখুন।"
                    }
                )
            }
            else -> {
                Triple(
                    "safe",
                    "✅ ভ্রমণ ও ট্র্যাকিংয়ের অনুকূল আবহাওয়া",
                    "বর্তমানে আবহাওয়া শান্ত ও স্বাভাবিক। চট্টগ্রামের সমুদ্র সৈকত, সাজেক বা বান্দরবান পাহাড় ভ্রমণের জন্য সম্পূর্ণ নিরাপদ ও চমৎকার পরিবেশ।"
                )
            }
        }
    }
}
