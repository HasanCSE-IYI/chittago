package com.hasan.chittago.ui.dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hasan.chittago.databinding.DialogEmergencyBinding
import com.hasan.chittago.databinding.ItemEmergencyBinding

data class EmergencyContact(
    val title: String,
    val subtitle: String,
    val phone: String
)

class EmergencyDirectoryDialog : BottomSheetDialogFragment() {

    private var _binding: DialogEmergencyBinding? = null
    private val binding get() = _binding!!

    private val contacts = listOf(
        EmergencyContact("জাতীয় জরুরি সেবা (৯৯৯)", "পুলিশ, অ্যাম্বুলেন্স ও ফায়ার সার্ভিস", "999"),
        EmergencyContact("ট্যুরিস্ট পুলিশ কক্সবাজার", "সমুদ্র সৈকত ও পর্যটক নিরাপত্তা হেল্পলাইন", "+8801320160120"),
        EmergencyContact("ট্যুরিস্ট পুলিশ বান্দরবান", "পাহাড়ি পর্যটন ও রিসোর্ট নিরাপত্তা", "+8801320163350"),
        EmergencyContact("চমেক (CMCH) জরুরি বিভাগ", "চট্টগ্রাম মেডিকেল কলেজ ও হাসপাতাল", "+88031616395"),
        EmergencyContact("চট্টগ্রাম ফায়ার সার্ভিস", "আগ্রাবাদ কেন্দ্রীয় কন্ট্রোল রুম", "+8801730322222"),
        EmergencyContact("রেড ক্রিসেন্ট অ্যাম্বুলেন্স", "জরুরি রোগী পরিবহন সেবা চট্টগ্রাম", "+8801819318181")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEmergencyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCloseEmergency.setOnClickListener { dismiss() }

        binding.rvEmergency.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEmergency.adapter = EmergencyAdapter(contacts) { phone ->
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class EmergencyAdapter(
        private val list: List<EmergencyContact>,
        private val onCallClick: (String) -> Unit
    ) : RecyclerView.Adapter<EmergencyAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemEmergencyBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemEmergencyBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.binding.tvEmergencyName.text = item.title
            holder.binding.tvEmergencySubtitle.text = item.subtitle
            holder.binding.btnEmergencyCall.setOnClickListener {
                onCallClick(item.phone)
            }
        }

        override fun getItemCount(): Int = list.size
    }

    companion object {
        const val TAG = "EmergencyDirectoryDialog"
    }
}
