package com.martinszuc.phishing_emails_detection.ui.component.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlTrainingBinding
import com.martinszuc.phishing_emails_detection.ui.component.training.adapter.TrainingSelectionAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.ProcessedPackageSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingFragment : Fragment() {

    private var _binding: FragmentMlTrainingBinding? = null
    private val binding get() = _binding!!
    private val processedPackageSharedViewModel: ProcessedPackageSharedViewModel by activityViewModels()

    private lateinit var trainingSelectionAdapter: TrainingSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMlTrainingBinding.inflate(inflater, container, false)
        setupRecyclerView()
        initFloatingActionButton()
        return binding.root
    }

    private fun setupRecyclerView() {
        trainingSelectionAdapter = TrainingSelectionAdapter { processedPackage, isChecked ->
            // Logic to handle selection
        }
        binding.rvProcessedPackages.layoutManager = LinearLayoutManager(context)
        binding.rvProcessedPackages.adapter = trainingSelectionAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeProcessedPackages()
        // Trigger loading of packages, depending on your ViewModel implementation
        processedPackageSharedViewModel.refreshAndLoadProcessedPackages()
    }

    private fun observeProcessedPackages() {
        processedPackageSharedViewModel.processedPackages.observe(viewLifecycleOwner) { packages ->
            trainingSelectionAdapter.setItems(packages.toList())
        }
    }

    private fun initFloatingActionButton() {
        val fab: FloatingActionButton = binding.fab
        // Implement FAB click action to proceed to the next step
        fab.setOnClickListener {
            // Logic to handle FAB click, e.g., navigate to training configuration or start training
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
