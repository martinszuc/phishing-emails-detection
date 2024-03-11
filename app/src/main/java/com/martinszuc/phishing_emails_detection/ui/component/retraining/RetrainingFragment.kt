package com.martinszuc.phishing_emails_detection.ui.component.retraining

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlRetrainingBinding
import com.martinszuc.phishing_emails_detection.ui.component.training.adapter.TrainingSelectionAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.ModelManagerSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.ProcessedPackageSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RetrainingFragment : Fragment() {

    private var _binding: FragmentMlRetrainingBinding? = null
    private val binding get() = _binding!!

    private val retrainingViewModel: RetrainingViewModel by activityViewModels()
    private val processedPackageSharedViewModel: ProcessedPackageSharedViewModel by activityViewModels()
    private val modelManagerSharedViewModel: ModelManagerSharedViewModel by activityViewModels()

    private lateinit var trainingSelectionAdapter: TrainingSelectionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMlRetrainingBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupModelSpinner()
        setupRetrainButton()
        return binding.root
    }

    private fun setupRecyclerView() {
        trainingSelectionAdapter = TrainingSelectionAdapter { processedPackage, isChecked ->
            retrainingViewModel.togglePackageSelected(processedPackage)
        }
        with(binding) {
            rvProcessedPackages.layoutManager = LinearLayoutManager(context)
            rvProcessedPackages.adapter = trainingSelectionAdapter
        }
        observeProcessedPackages()
    }

    private fun setupModelSpinner() {
        val modelSpinner: Spinner = binding.modelSpinner
        modelManagerSharedViewModel.models.observe(viewLifecycleOwner) { models ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, models.map { it.modelName })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            modelSpinner.adapter = adapter

            modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedModel = models[position]
                    retrainingViewModel.toggleSelectedModel(selectedModel)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // This can be left empty or used to handle any cleanup if necessary when nothing is selected
                }
            }
        }
    }

    private fun setupRetrainButton() {
        binding.fab.setOnClickListener {
            // This is where you call the startModelRetraining from the RetrainingViewModel
            retrainingViewModel.startModelRetraining()
        }
    }

    private fun observeProcessedPackages() {
        processedPackageSharedViewModel.processedPackages.observe(viewLifecycleOwner) { packages ->
            trainingSelectionAdapter.setItems(packages.toList())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
