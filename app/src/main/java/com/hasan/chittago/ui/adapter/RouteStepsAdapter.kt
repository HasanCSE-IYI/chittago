package com.hasan.chittago.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hasan.chittago.R
import com.hasan.chittago.data.model.RouteStep
import com.hasan.chittago.databinding.ItemRouteStepBinding
import com.hasan.chittago.util.GeoUtils

class RouteStepsAdapter : ListAdapter<RouteStep, RouteStepsAdapter.ViewHolder>(StepDiffCallback()) {

    class ViewHolder(val binding: ItemRouteStepBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRouteStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val step = getItem(position)
        holder.binding.tvStepInstruction.text = step.instruction
        val distText = if (step.distanceMeters > 0) {
            GeoUtils.formatDistance(step.distanceMeters / 1000.0)
        } else {
            "অবস্থান"
        }
        holder.binding.tvStepDistance.text = distText

        val iconRes = when {
            step.type == "arrive" -> R.drawable.ic_arrive
            step.modifier.contains("left") -> R.drawable.ic_turn_left
            step.modifier.contains("right") -> R.drawable.ic_turn_right
            else -> R.drawable.ic_straight
        }
        holder.binding.ivStepIcon.setImageResource(iconRes)
    }

    class StepDiffCallback : DiffUtil.ItemCallback<RouteStep>() {
        override fun areItemsTheSame(oldItem: RouteStep, newItem: RouteStep): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: RouteStep, newItem: RouteStep): Boolean {
            return oldItem == newItem
        }
    }
}
