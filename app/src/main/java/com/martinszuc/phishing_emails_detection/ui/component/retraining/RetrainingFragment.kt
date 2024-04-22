package com.martinszuc.phishing_emails_detection.ui.component.retraining

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.model_manager.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlRetrainingBinding
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseFragment
import com.martinszuc.phishing_emails_detection.ui.component.training.adapter.TrainingSelectionAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.ModelManagerSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.ProcessedPackageSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import java.util.Date

@AndroidEntryPoint
class RetrainingFragment : AbstractBaseFragment() {

    private var _binding: FragmentMlRetrainingBinding? = null
    private val binding get() = _binding!!

    private val retrainingViewModel: RetrainingViewModel by activityViewModels()
    private val processedPackageSharedViewModel: ProcessedPackageSharedViewModel by activityViewModels()
    private val modelManagerSharedViewModel: ModelManagerSharedViewModel by activityViewModels()

    private lateinit var trainingSelectionAdapter: TrainingSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMlRetrainingBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupRetrainButton()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeModels()
        observeLoading()
    }

    override fun onResume() {
        super.onResume()
        processedPackageSharedViewModel.refreshAndLoadProcessedPackages()
    }

    private fun setupRecyclerView() {
        trainingSelectionAdapter = TrainingSelectionAdapter { processedPackage, _ ->
            retrainingViewModel.togglePackageSelected(processedPackage)
        }
        with(binding) {
            rvProcessedPackages.layoutManager = LinearLayoutManager(context)
            rvProcessedPackages.adapter = trainingSelectionAdapter
        }
        observeProcessedPackages()
    }


    private fun observeModels() {
        modelManagerSharedViewModel.refreshAndLoadModels()
        modelManagerSharedViewModel.models.observe(viewLifecycleOwner) { models ->
            setupModelSpinner(models)
        }
    }

    private fun setupModelSpinner(models: List<ModelMetadata>) {
        // Create a mutable list to modify the data
        val spinnerModels = mutableListOf<ModelMetadata>().apply {
            // Add a default "prompt" item at the beginning of the list
            add(
                ModelMetadata(
                    "Please pick one of your models",
                    Date(0)
                )
            ) // Date(0) just as a placeholder
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
                        retrainingViewModel.toggleSelectedModel(selectedModel)
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

    private fun observeLoading() {
        retrainingViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            val progressBar = binding.progressBar
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        }

        retrainingViewModel.isFinished.observe(viewLifecycleOwner) { isFinished ->
            if (isFinished) {
                // Show the dialog with options
                showFinishTrainingDialog()
                // Optionally, hide the ProgressBar when finished
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showFinishTrainingDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_finished_training, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true) // Prevent the dialog from being dismissed by back press or touches outside
            .create()

        // Find and set up the buttons from the dialog layout
        dialogView.findViewById<Button>(R.id.btnGoToModelManager).setOnClickListener {
            dialog.dismiss()
            // Navigate to ModelManagerFragment
            val navController = findNavController()
            navController.navigate(R.id.action_trainingFragment_to_modelManagerFragment) // Use the correct action ID
        }

        dialogView.findViewById<Button>(R.id.btnDismiss).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        binding.rvProcessedPackages.adapter = null
        lifecycleScope.cancel()
        super.onDestroyView()
        _binding = null
    }
}
