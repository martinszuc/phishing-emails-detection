package com.martinszuc.phishing_emails_detection.ui.component.machine_learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.martinszuc.phishing_emails_detection.databinding.FragmentMachineLearningBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MachineLearningParentFragment : Fragment() {              // TODO back button and skip button for data picker to training/retraining

    private var _binding: FragmentMachineLearningBinding? = null
    private val binding get() = _binding!!

    private val machineLearningParentSharedViewModel: MachineLearningParentSharedViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMachineLearningBinding.inflate(inflater, container, false)

        setupViewPager()
        setupTabLayout()
        observeViewModelState()

        return binding.root
    }

    private fun setupViewPager() {
        val adapter = MachineLearningPagerAdapter(this)
        binding.machineLearningViewpager.apply {
            this.adapter = adapter
            isUserInputEnabled = false
        }
    }

    private fun setupTabLayout() {
        val tabTitles = listOf("Data Processing", "Training", "Retraining")
        tabTitles.forEach { title ->
            binding.machineLearningTabs.addTab(binding.machineLearningTabs.newTab().setText(title))

        }

        // Disable tab click navigation
        for (i in 0 until binding.machineLearningTabs.tabCount) {
            val tab = (binding.machineLearningTabs.getChildAt(0) as ViewGroup).getChildAt(i)
            tab.setOnTouchListener { _, _ -> true } // Override the touch listener to do nothing
        }

        // Adjust tab selection to reflect ViewPager page changes
        binding.machineLearningViewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position > 0) { // Adjust for "Data Picking" not having a visible tab
                    binding.machineLearningTabs.getTabAt(position - 1)?.select()
                }
            }
        })
    }

    private fun observeViewModelState() {
        machineLearningParentSharedViewModel.state.observe(viewLifecycleOwner) { state ->
            val currentItem = when (state) {
                MachineLearningState.DATA_PICKING -> 0
                MachineLearningState.DATA_PROCESSING -> 1
                MachineLearningState.TRAINING -> 2
                MachineLearningState.RETRAINING -> 3
            }
            binding.machineLearningViewpager.currentItem = currentItem
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        machineLearningParentSharedViewModel.setState(MachineLearningState.DATA_PICKING) // Or any default state
        _binding = null // Avoid memory leaks
    }
}
