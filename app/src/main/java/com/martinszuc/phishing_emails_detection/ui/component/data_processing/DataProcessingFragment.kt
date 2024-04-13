package com.martinszuc.phishing_emails_detection.ui.component.data_processing

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlDataProcessingBinding
import com.martinszuc.phishing_emails_detection.ui.component.data_picking.DataPickingViewModel
import com.martinszuc.phishing_emails_detection.ui.component.machine_learning.MachineLearningParentSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.component.machine_learning.MachineLearningState
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.ProcessedPackageSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DataProcessingFragment : Fragment() {  // TODO already processed data to show here and being able to choose
    // TODO fix loading

    private var _binding: FragmentMlDataProcessingBinding? = null
    private val binding get() = _binding!!
    private val dataProcessingViewModel: DataProcessingViewModel by viewModels()
    private val dataPickingViewModel: DataPickingViewModel by activityViewModels()
    private val machineLearningParentSharedViewModel: MachineLearningParentSharedViewModel by activityViewModels()
    private val processedPackageSharedViewModel: ProcessedPackageSharedViewModel by activityViewModels()

    private var processingStarted = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMlDataProcessingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initFloatingActionButton()

        binding.btnProcessEmails.setOnClickListener {
            val selectedPackages = dataPickingViewModel.selectedPackages.value ?: setOf()
            if (selectedPackages.isNotEmpty()) {
                processingStarted = true
                binding.ivCheck.visibility = View.GONE  // Ensure check icon is hidden when starting
                processPackages(selectedPackages)
            } else {
                Toast.makeText(context, "No packages selected", Toast.LENGTH_SHORT).show()
            }
        }

        dataProcessingViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                // Show the loading spinner and text
                binding.progressBar.visibility = View.VISIBLE
                binding.tvLoading.visibility = View.VISIBLE
                // Disable the button while processing
                binding.btnProcessEmails.isEnabled = false
            } else {
                // Hide the loading spinner and text
                binding.progressBar.visibility = View.GONE
                binding.tvLoading.visibility = View.GONE
                // Enable the button after processing
                binding.btnProcessEmails.isEnabled = true
            }
        }

        dataProcessingViewModel.isFinished.observe(viewLifecycleOwner) { isFinished ->
            if (isFinished && processingStarted) {
                // Show the check icon if processing has finished and was started by the button press
                binding.ivCheck.visibility = View.VISIBLE
                binding.fab.visibility = View.VISIBLE

                processedPackageSharedViewModel.refreshAndLoadProcessedPackages()

                processingStarted = false // Reset the flag after handling
                binding.btnProcessEmails.isEnabled = false
            }
        }
    }

    private fun processPackages(packages: Set<EmailPackageMetadata>) {
        dataProcessingViewModel.processEmailPackages(packages)
    }

    private fun initFloatingActionButton() {
        val fab: FloatingActionButton = binding.fab
        fab.setOnClickListener {
            showTrainingOptionDialog()
        }
    }

    private fun showTrainingOptionDialog() {
        val context = requireContext()
        AlertDialog.Builder(context).apply {
            setTitle("Select Option")
            setMessage("Do you wish to train a new model or retrain an existing model?")
            setPositiveButton("Train New") { _, _ ->
                // User chooses to train a new model
                machineLearningParentSharedViewModel.setState(MachineLearningState.TRAINING)
            }
            setNegativeButton("Retrain Existing") { _, _ ->
                // User chooses to retrain an existing model
                machineLearningParentSharedViewModel.setState(MachineLearningState.RETRAINING)
            }
            setCancelable(true)
        }.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dataProcessingViewModel.clearIsFinished()
        _binding = null
    }
}
