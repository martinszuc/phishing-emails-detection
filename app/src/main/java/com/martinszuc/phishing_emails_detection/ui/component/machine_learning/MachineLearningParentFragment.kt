package com.martinszuc.phishing_emails_detection.ui.component.machine_learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.martinszuc.phishing_emails_detection.databinding.FragmentMachineLearningBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MachineLearningParentFragment : Fragment() {

    private var _binding: FragmentMachineLearningBinding? = null
    private val binding get() = _binding!!

    private val machineLearningParentSharedViewModel: MachineLearningParentSharedViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMachineLearningBinding.inflate(inflater, container, false)

        val adapter = MachineLearningPagerAdapter(this)
        binding.machineLearningViewpager.adapter = adapter
        binding.machineLearningViewpager.isUserInputEnabled = false // TODO verify

        machineLearningParentSharedViewModel.state.observe(viewLifecycleOwner) { state ->
            val position = when (state) {
                MachineLearningState.DATA_PICKING -> 0
                MachineLearningState.DATA_PROCESSING -> 1
                MachineLearningState.TRAINING -> 2
                MachineLearningState.RETRAINING -> 3
            }
            binding.machineLearningViewpager.currentItem = position
        }

        // Setup TabLayout with ViewPager2
        val tabTitles = listOf("Data Picking", "Data Processing", "Training", "Retraining")
        TabLayoutMediator(binding.machineLearningTabs, binding.machineLearningViewpager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // Disable tab click navigation
        for (i in 0 until binding.machineLearningTabs.tabCount) {
            val tab = (binding.machineLearningTabs.getChildAt(0) as ViewGroup).getChildAt(i)
            tab.setOnTouchListener { _, _ -> true } // Override the touch listener to do nothing
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        machineLearningParentSharedViewModel.setState(MachineLearningState.DATA_PICKING) // Or any default state
        _binding = null
    }
}
