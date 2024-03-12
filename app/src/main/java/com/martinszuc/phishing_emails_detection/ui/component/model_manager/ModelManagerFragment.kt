package com.martinszuc.phishing_emails_detection.ui.component.model_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.martinszuc.phishing_emails_detection.data.model_manager.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.databinding.FragmentModelManagerBinding
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.ModelManagerSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class ModelManagerFragment : Fragment() {
    private var _binding: FragmentModelManagerBinding? = null
    private val modelManagerSharedViewModel: ModelManagerSharedViewModel by activityViewModels()

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentModelManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe model data from ViewModel
        observeModels()

        // Set up FAB click listener to refresh and load models
        setupFabRefresh()

    }

    private fun observeModels() {
        modelManagerSharedViewModel.models.observe(viewLifecycleOwner) { models ->
            setupModelSpinner(models)
        }
    }

    private fun setupFabRefresh() {
        binding.fab.setOnClickListener {
            modelManagerSharedViewModel.refreshAndLoadModels()
            Toast.makeText(requireContext(), "Refreshing models...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupModelSpinner(models: List<ModelMetadata>) {
        // Create a mutable list to modify the data
        val spinnerModels = mutableListOf<ModelMetadata>().apply {
            // Add a default "prompt" item at the beginning of the list
            add(ModelMetadata("Please pick one of your models", Date(0))) // Date(0) just as a placeholder
            addAll(models)
        }

        // Adapter setup with the modified list, using a custom layout if necessary
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerModels.map { it.modelName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerModelSelector.adapter = adapter

        binding.spinnerModelSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Ignore the default item selection
                if (position > 0) {
                    val selectedModel = spinnerModels[position]
                    modelManagerSharedViewModel.toggleSelectedModel(selectedModel)
                    Toast.makeText(requireContext(), "Selected: ${selectedModel.modelName}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: Handle the case where nothing is selected
            }
        }

        // Initially set the spinner to show the default item
        binding.spinnerModelSelector.setSelection(0)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
