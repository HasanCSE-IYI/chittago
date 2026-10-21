package com.hasan.chittago.ui.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hasan.chittago.R
import com.hasan.chittago.data.model.WeatherInfo
import com.hasan.chittago.databinding.DialogWeatherAdvisoryBinding
import java.util.Locale

class WeatherAdvisoryDialog(
    private val weather: WeatherInfo
) : BottomSheetDialogFragment() {

    private var _binding: DialogWeatherAdvisoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogWeatherAdvisoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCloseWeather.setOnClickListener { dismiss() }

        binding.tvWeatherDistrict.text = weather.districtName
        binding.tvWeatherCondition.text = weather.conditionBn
        binding.tvWeatherTemp.text = String.format(Locale.US, "%.1f°C", weather.temperatureC)
        binding.tvWeatherHighLow.text = String.format(
            Locale.US,
            "সর্বোচ্চ %.0f° • সর্বনিম্ন %.0f°",
            weather.dailyMaxTemp,
            weather.dailyMinTemp
        )
        binding.tvWeatherRainProb.text = "বৃষ্টির সম্ভাবনা: ${weather.rainProbability}%"
        binding.tvWeatherWind.text = String.format(Locale.US, "%.1f কিমি/ঘ", weather.windSpeedKmh)
        binding.tvWeatherHumidity.text = "${weather.humidityPercent}%"
        binding.tvWeatherPrecip.text = String.format(Locale.US, "%.1f মিমি", weather.precipitationMm)

        // Advisory Styling
        binding.tvAdvisoryTitle.text = weather.advisoryTitleBn
        binding.tvAdvisoryMessage.text = weather.advisoryMessageBn

        when (weather.advisoryLevel) {
            "danger" -> {
                binding.cardAdvisory.setCardBackgroundColor(Color.parseColor("#FEE2E2"))
                binding.tvAdvisoryTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.coral))
            }
            "moderate" -> {
                binding.cardAdvisory.setCardBackgroundColor(Color.parseColor("#FEF3C7"))
                binding.tvAdvisoryTitle.setTextColor(Color.parseColor("#D97706"))
            }
            else -> {
                binding.cardAdvisory.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.emerald_light))
                binding.tvAdvisoryTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.emerald))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "WeatherAdvisoryDialog"
    }
}
