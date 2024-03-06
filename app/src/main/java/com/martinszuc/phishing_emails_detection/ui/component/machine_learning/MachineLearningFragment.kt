package com.martinszuc.phishing_emails_detection.ui.component.machine_learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.martinszuc.phishing_emails_detection.databinding.FragmentMachineLearningBinding
import com.martinszuc.phishing_emails_detection.ui.component.data_picking.DataPickingFragment
import com.martinszuc.phishing_emails_detection.ui.component.data_processing.DataProcessingFragment
import com.martinszuc.phishing_emails_detection.ui.component.retraining.RetrainingFragment
import com.martinszuc.phishing_emails_detection.ui.component.training.TrainingFragment
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.machine_learning.MachineLearningSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.machine_learning.MachineLearningState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MachineLearningFragment : Fragment() {

    private var _binding: FragmentMachineLearningBinding? = null
    private val binding get() = _binding!!

    private val machineLearningSharedViewModel: MachineLearningSharedViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMachineLearningBinding.inflate(inflater, container, false)

        val adapter = MachineLearningPagerAdapter(this)
        binding.machineLearningViewpager.adapter = adapter

        // Optionally, if you want to navigate to a specific page based on a state change
        machineLearningSharedViewModel.state.observe(viewLifecycleOwner) { state ->
            val position = when (state) {
                MachineLearningState.DATA_PICKING -> 0
                MachineLearningState.DATA_PROCESSING -> 1
                MachineLearningState.TRAINING -> 2
                MachineLearningState.RETRAINING -> 3
            }
            binding.machineLearningViewpager.currentItem = position
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
