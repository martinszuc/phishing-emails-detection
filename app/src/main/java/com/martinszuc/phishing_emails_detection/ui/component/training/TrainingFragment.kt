package com.martinszuc.phishing_emails_detection.ui.component.training

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlTrainingBinding
import com.martinszuc.phishing_emails_detection.ui.component.machine_learning.MachineLearningParentSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.component.machine_learning.MachineLearningState
import com.martinszuc.phishing_emails_detection.ui.component.training.adapter.TrainingSelectionAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.ProcessedPackageSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class TrainingFragment : Fragment() {

    private var _binding: FragmentMlTrainingBinding? = null
    private val binding get() = _binding!!
    private val processedPackageSharedViewModel: ProcessedPackageSharedViewModel by activityViewModels()
    private val machineLearningParentSharedViewModel: MachineLearningParentSharedViewModel by activityViewModels()
    private val trainingViewModel: TrainingViewModel by activityViewModels()

    private lateinit var trainingSelectionAdapter: TrainingSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMlTrainingBinding.inflate(inflater, container, false)
        setupRecyclerView()
        initFloatingActionButton()
        initBackFloatingActionButton()
        return binding.root
    }

    private fun setupRecyclerView() {
        trainingSelectionAdapter = TrainingSelectionAdapter { processedPackage, isChecked ->
            trainingViewModel.togglePackageSelected(processedPackage)
        }
        binding.rvProcessedPackages.layoutManager = LinearLayoutManager(context)
        binding.rvProcessedPackages.adapter = trainingSelectionAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeProcessedPackages()
        // Trigger loading of packages, depending on your ViewModel implementation
        processedPackageSharedViewModel.refreshAndLoadProcessedPackages()

        setupObservers()
    }

    private fun observeProcessedPackages() {
        processedPackageSharedViewModel.processedPackages.observe(viewLifecycleOwner) { packages ->
            trainingSelectionAdapter.setItems(packages.toList())
        }
    }

    private fun initFloatingActionButton() {
        val fab: ExtendedFloatingActionButton = binding.fab
        // Implement FAB click action to proceed to the next step
        fab.setOnClickListener {
            lifecycleScope.launch {
                val modelName = showModelNameInputDialog()
                modelName?.let {
                    // Pass the model name to the ViewModel
                    trainingViewModel.startModelTraining(it)
                }
            }
        }
    }

    private fun initBackFloatingActionButton() {
        val fab: ExtendedFloatingActionButton = binding.fabLeft
        // Implement FAB click action to proceed to the next step
        fab.setOnClickListener {
            machineLearningParentSharedViewModel.setState(MachineLearningState.DATA_PICKING)
        }
    }

    private suspend fun showModelNameInputDialog(): String? = suspendCoroutine { cont ->
        val context = requireContext()
        val input = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = "Enter model name"
        }

        val dialog = AlertDialog.Builder(context).apply {
            setTitle("Enter Model Name")
            setMessage("Please enter a name for the new model:")
            setView(input) // Add the input field to the dialog
            setPositiveButton("OK") { _, _ ->
                cont.resume(input.text.toString())
            }
            setNegativeButton("Cancel") { _, _ ->
                cont.resume(null) // Resume with null if the user cancels the dialog
            }
            setCancelable(false) // Make dialog non-cancelable to ensure input
            setOnCancelListener {
                cont.resume(null) // Resume with null if the dialog is canceled
            }
        }.create()

        dialog.show()
    }

    private fun setupObservers() {
        trainingViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            val progressBar = binding.progressBar
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        }

        trainingViewModel.isFinished.observe(viewLifecycleOwner) { isFinished ->
            if (isFinished) {
                // Show the dialog with options
                showFinishTrainingDialog()
                // Optionally, hide the ProgressBar when finished
                binding.progressBar.visibility = View.GONE
            } else {
            }
        }
    }

    private fun showFinishTrainingDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_finished_training, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false) // Prevent the dialog from being dismissed by back press or touches outside
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
        super.onDestroyView()
        _binding = null
    }
}
