package com.hasan.chittago.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hasan.chittago.data.model.RouteStep
import com.hasan.chittago.databinding.DialogRouteStepsBinding
import com.hasan.chittago.ui.adapter.RouteStepsAdapter

class RouteStepsDialog(
    private val steps: List<RouteStep>,
    private val summary: String
) : BottomSheetDialogFragment() {

    private var _binding: DialogRouteStepsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRouteStepsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvStepsSummary.text = summary
        binding.btnCloseSteps.setOnClickListener { dismiss() }

        val adapter = RouteStepsAdapter()
        binding.rvSteps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSteps.adapter = adapter
        adapter.submitList(steps)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RouteStepsDialog"
    }
}
