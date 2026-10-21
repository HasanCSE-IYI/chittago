package com.hasan.chittago.ui.dialog

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hasan.chittago.R
import com.hasan.chittago.databinding.DialogMapLayersBinding

class MapLayersDialog(
    private var selectedLayer: String,
    private val isRadarActive: Boolean,
    private val isPoiActive: Boolean,
    private val isScaleActive: Boolean,
    private val onLayerSelected: (String) -> Unit,
    private val onRadarToggled: (Boolean) -> Unit,
    private val onPoiToggled: (Boolean) -> Unit,
    private val onScaleToggled: (Boolean) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogMapLayersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMapLayersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCloseLayers.setOnClickListener { dismiss() }

        // Setup switch initial states
        binding.switchRainRadar.isChecked = isRadarActive
        binding.switchPoiPins.isChecked = isPoiActive
        binding.switchScaleBar.isChecked = isScaleActive

        // Switch listeners
        binding.switchRainRadar.setOnCheckedChangeListener { _, isChecked ->
            onRadarToggled(isChecked)
        }
        binding.switchPoiPins.setOnCheckedChangeListener { _, isChecked ->
            onPoiToggled(isChecked)
        }
        binding.switchScaleBar.setOnCheckedChangeListener { _, isChecked ->
            onScaleToggled(isChecked)
        }

        // Layer selection cards
        updateSelectedLayerUI(selectedLayer)

        binding.btnLayerOsm.setOnClickListener {
            selectedLayer = "osm"
            updateSelectedLayerUI("osm")
            onLayerSelected("osm")
        }

        binding.btnLayerHybrid.setOnClickListener {
            selectedLayer = "hybrid"
            updateSelectedLayerUI("hybrid")
            onLayerSelected("hybrid")
        }

        binding.btnLayerTopo.setOnClickListener {
            selectedLayer = "topo"
            updateSelectedLayerUI("topo")
            onLayerSelected("topo")
        }

        binding.btnLayerCycle.setOnClickListener {
            selectedLayer = "cycle"
            updateSelectedLayerUI("cycle")
            onLayerSelected("cycle")
        }

        binding.btnLayerDark.setOnClickListener {
            selectedLayer = "dark"
            updateSelectedLayerUI("dark")
            onLayerSelected("dark")
        }
    }

    private fun updateSelectedLayerUI(activeLayer: String) {
        val white = ContextCompat.getColor(requireContext(), R.color.surface_white)
        val textPrimary = ContextCompat.getColor(requireContext(), R.color.text_primary)
        val accentTeal = ContextCompat.getColor(requireContext(), R.color.accent_teal)
        val emerald = ContextCompat.getColor(requireContext(), R.color.emerald)
        val coral = ContextCompat.getColor(requireContext(), R.color.coral)
        val primary = ContextCompat.getColor(requireContext(), R.color.primary)

        val cards = listOf(
            Triple("osm", binding.btnLayerOsm, Pair(binding.ivLayerOsmIcon, binding.tvLayerOsmTitle)),
            Triple("hybrid", binding.btnLayerHybrid, Pair(binding.ivLayerHybridIcon, binding.tvLayerHybridTitle)),
            Triple("topo", binding.btnLayerTopo, Pair(binding.ivLayerTopoIcon, binding.tvLayerTopoTitle)),
            Triple("cycle", binding.btnLayerCycle, Pair(binding.ivLayerCycleIcon, binding.tvLayerCycleTitle)),
            Triple("dark", binding.btnLayerDark, Pair(binding.ivLayerDarkIcon, binding.tvLayerDarkTitle))
        )

        for ((type, container, iconAndText) in cards) {
            val (icon, title) = iconAndText
            if (type == activeLayer) {
                container.setBackgroundResource(R.drawable.bg_chip_selected)
                icon.imageTintList = ColorStateList.valueOf(white)
                title.setTextColor(white)
            } else {
                container.setBackgroundResource(R.drawable.bg_chip_unselected)
                val unselectedTint = when (type) {
                    "osm" -> accentTeal
                    "hybrid" -> accentTeal
                    "topo" -> emerald
                    "cycle" -> coral
                    "dark" -> primary
                    else -> accentTeal
                }
                icon.imageTintList = ColorStateList.valueOf(unselectedTint)
                title.setTextColor(textPrimary)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "MapLayersDialog"
    }
}
