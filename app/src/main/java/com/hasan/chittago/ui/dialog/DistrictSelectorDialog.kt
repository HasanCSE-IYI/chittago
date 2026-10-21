package com.hasan.chittago.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hasan.chittago.databinding.DialogDistrictSelectorBinding
import com.hasan.chittago.ui.adapter.DistrictAdapter
import com.hasan.chittago.ui.adapter.DistrictItem

class DistrictSelectorDialog(
    private val onDistrictSelected: (DistrictItem) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogDistrictSelectorBinding? = null
    private val binding get() = _binding!!

    private val districts = listOf(
        DistrictItem("চট্টগ্রাম (মেট্রো ও বন্দর)", "Chattogram Metropolitan", 22.3569, 91.8286, "⚓", "বন্দরনগরী ও প্রধান প্রশাসনিক কেন্দ্র"),
        DistrictItem("কক্সবাজার", "Cox's Bazar", 21.4272, 91.9806, "🏖️", "বিশ্বের দীর্ঘতম সমুদ্র সৈকত ও প্রবাল দ্বীপ"),
        DistrictItem("বান্দরবান", "Bandarban", 22.1953, 92.2215, "⛰️", "নীলগিরি, নীলাচল ও মেঘের উপত্যকা"),
        DistrictItem("রাঙ্গামাটি", "Rangamati", 22.6533, 92.1753, "🛶", "কাপ্তাই হ্রদ ও ঝুলন্ত সেতু"),
        DistrictItem("খাগড়াছড়ি", "Khagrachhari", 23.1193, 91.9847, "🌲", "সাজেক ভ্যালি ও আলুটিলা রহস্যময় গুহা"),
        DistrictItem("কুমিল্লা", "Cumilla", 23.4682, 91.1788, "🏛️", "শালবন বৌদ্ধ বিহার ও ময়নামতি প্রত্নতত্ত্ব"),
        DistrictItem("ফেনী", "Feni", 23.0159, 91.3976, "🌊", "মুহুরী সেচ প্রকল্প ও সিলোনিয়া নদী"),
        DistrictItem("নোয়াখালী", "Noakhali", 22.8696, 91.0994, "🦌", "নিঝুম দ্বীপ ও উপকূলীয় বনাঞ্চল"),
        DistrictItem("লক্ষ্মীপুর", "Lakshmipur", 22.9447, 90.8282, "🌅", "রামগতি মেঘনা সৈকত ও দালাল বাজার জমিদার বাড়ি"),
        DistrictItem("চাঁদপুর", "Chandpur", 23.2323, 90.6433, "🐟", "পদ্মা-মেঘনা-ডাকাতিয়ার ত্রিনদী মোহনা"),
        DistrictItem("ব্রাহ্মণবাড়িয়া", "Brahmanbaria", 23.9608, 91.1115, "🕌", "খাড়মপুর মাজার ও তিতাস গ্যাস অঞ্চল")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDistrictSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDistricts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDistricts.adapter = DistrictAdapter(districts) { district ->
            onDistrictSelected(district)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DistrictSelectorDialog"
    }
}
