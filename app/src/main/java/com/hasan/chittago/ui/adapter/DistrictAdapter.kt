package com.hasan.chittago.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hasan.chittago.databinding.ItemDistrictBinding

data class DistrictItem(
    val nameBn: String,
    val nameEn: String,
    val lat: Double,
    val lng: Double,
    val emoji: String,
    val subtitle: String
)

class DistrictAdapter(
    private val districts: List<DistrictItem>,
    private val onDistrictClick: (DistrictItem) -> Unit
) : RecyclerView.Adapter<DistrictAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDistrictBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDistrictBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = districts[position]
        holder.binding.tvDistrictEmoji.text = item.emoji
        holder.binding.tvDistrictNameBn.text = item.nameBn
        holder.binding.tvDistrictNameEn.text = item.subtitle

        holder.itemView.setOnClickListener {
            onDistrictClick(item)
        }
    }

    override fun getItemCount(): Int = districts.size
}
