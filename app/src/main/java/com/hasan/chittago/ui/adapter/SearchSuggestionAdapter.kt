package com.hasan.chittago.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hasan.chittago.data.model.PlacePOI
import com.hasan.chittago.databinding.ItemSearchSuggestionBinding

class SearchSuggestionAdapter(
    private var items: List<PlacePOI>,
    private val onItemClick: (PlacePOI) -> Unit
) : RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSearchSuggestionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchSuggestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val poi = items[position]
        holder.binding.tvSuggestionName.text = poi.nameBn.ifEmpty { poi.name }
        holder.binding.tvSuggestionSubtitle.text = poi.address.ifEmpty { poi.district }
        holder.binding.tvSuggestionDistrict.text = poi.district

        holder.binding.tvSuggestionIcon.text = when (poi.category) {
            "hospital" -> "🏥"
            "mezban" -> "🍲"
            "transport" -> "🚌"
            "emergency" -> "🚨"
            else -> "🏖️"
        }

        holder.itemView.setOnClickListener {
            onItemClick(poi)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<PlacePOI>) {
        items = newItems
        notifyDataSetChanged()
    }
}
