package com.martinszuc.phishing_emails_detection.ui.component.model_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.databinding.FragmentModelManagerBinding
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseFragment
import com.martinszuc.phishing_emails_detection.ui.component.machine_learning.evaluate_model.EvaluateModelFragment
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.ModelManagerSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class ModelManagerFragment : AbstractBaseFragment() {
    private var _binding: FragmentModelManagerBinding? = null
    private val modelManagerSharedViewModel: ModelManagerSharedViewModel by activityViewModels()
    private val modelManagerViewModel: ModelManagerViewModel by activityViewModels()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModelManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeModels()
        setupFabRefresh()
        setupButtonListeners()
        setupEvaluationButton()
    }

    private fun observeModels() {
        modelManagerSharedViewModel.models.observe(viewLifecycleOwner) { models ->
            setupModelSpinner(models)
        }
    }

    private fun setupFabRefresh() {
        binding.fab.setOnClickListener {
            modelManagerSharedViewModel.refreshAndLoadModels()
            showToast("Refreshing models...")
        }
    }

    private fun setupButtonListeners() {
        binding.btnExtractSendWeights.setOnClickListener {
            // Check if a model is selected before attempting to upload weights
            modelManagerViewModel.selectedModel.value?.let { modelMetadata ->
                modelManagerViewModel.uploadModelWeights()
                showToast("Uploading weights for ${modelMetadata.modelName}...")
            } ?: showToast("Please select a model first")
        }

        binding.btnLoadFromServer.setOnClickListener {
            // Check if a model is selected before attempting to download weights
            modelManagerViewModel.selectedModel.value?.let { modelMetadata ->
                modelManagerViewModel.downloadAndUpdateModelWeights()
                showToast("Downloading weights for ${modelMetadata.modelName}...")
            } ?: showToast("Please select a model first")
        }

        binding.btnDeleteModel.setOnClickListener {
            modelManagerViewModel.selectedModel.value?.let { modelMetadata ->
                modelManagerViewModel.deleteModelDirectory()
                showToast("Deleting model: ${modelMetadata.modelName}...")
                modelManagerSharedViewModel.refreshAndLoadModels()
            } ?: showToast("Please select a model first")
        }
    }

    private fun setupModelSpinner(models: List<ModelMetadata>) {
        val spinnerModels = mutableListOf<ModelMetadata>().apply {
            add(ModelMetadata("Please pick one of your models", Date(0))) // Placeholder
            addAll(models)
        }

        // Adapter setup with the modified list, using a custom layout if necessary
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            spinnerModels.map { it.modelName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerModelSelector.adapter = adapter

        binding.spinnerModelSelector.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // Ignore the default item selection
                    if (position > 0) {
                        val selectedModel = spinnerModels[position]
                        modelManagerViewModel.toggleSelectedModel(selectedModel)
                        showToast("Selected: ${selectedModel.modelName}")
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Optional: Handle the case where nothing is selected
                }
            }

        // Initially set the spinner to show the default item
        binding.spinnerModelSelector.setSelection(0)
    }

    private fun setupEvaluationButton() {
        binding.btnEvaluateModel.setOnClickListener {
            val selectedModel = modelManagerViewModel.selectedModel.value
            if (selectedModel == null) {
                showToast("Please select a model first")
            } else {
                EvaluateModelFragment().show(parentFragmentManager, "EvaluateModelFragment")
            }
        }
    }

    override fun onDestroyView() {
        binding.spinnerModelSelector.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
