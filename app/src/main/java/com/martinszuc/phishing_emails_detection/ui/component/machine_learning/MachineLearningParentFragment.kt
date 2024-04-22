package com.martinszuc.phishing_emails_detection.ui.component.machine_learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.martinszuc.phishing_emails_detection.databinding.FragmentMachineLearningBinding
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class MachineLearningParentFragment : AbstractBaseFragment() {
    private var _binding: FragmentMachineLearningBinding? = null
    private val binding get() = _binding!!

    private val machineLearningParentSharedViewModel: MachineLearningParentSharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMachineLearningBinding.inflate(inflater, container, false)

        setupViewPager()
        setupTabLayout()
        observeViewModelState()

        return binding.root
    }

    private fun setupViewPager() {
        val adapter = MachineLearningPagerAdapter(this)
        binding.machineLearningViewpager.adapter = adapter
        binding.machineLearningViewpager.isUserInputEnabled =
            true  // Allow user input for custom behavior

        binding.machineLearningViewpager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Adjust the machine learning state based on the selected page
                val newState = when (position) {
                    0 -> MachineLearningState.DATA_PICKING
                    1 -> MachineLearningState.TRAINING
                    2 -> MachineLearningState.RETRAINING
                    else -> return
                }
                machineLearningParentSharedViewModel.setState(newState)
                binding.machineLearningTabs.getTabAt(position)
                    ?.select()  // Ensure tabs are synchronized with page swipes
            }
        })
    }

    private fun setupTabLayout() {
        val tabTitles =
            listOf("Data Picking", "Training", "Retraining")  // Adjusted to remove Data Processing
        tabTitles.forEachIndexed { index, title ->
            binding.machineLearningTabs.addTab(
                binding.machineLearningTabs.newTab().setText(title),
                index
            )
        }

        binding.machineLearningTabs.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // Map directly to the pages which are available
                binding.machineLearningViewpager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun observeViewModelState() {
        machineLearningParentSharedViewModel.state.observe(viewLifecycleOwner) { state ->
            val viewPagerItem = when (state) {
                MachineLearningState.DATA_PICKING -> 0
                MachineLearningState.TRAINING -> 1
                MachineLearningState.RETRAINING -> 2
            }
            if (binding.machineLearningViewpager.currentItem != viewPagerItem) {
                binding.machineLearningViewpager.currentItem = viewPagerItem
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.machineLearningViewpager.adapter = null  // Clear the adapter
        _binding = null
    }
}