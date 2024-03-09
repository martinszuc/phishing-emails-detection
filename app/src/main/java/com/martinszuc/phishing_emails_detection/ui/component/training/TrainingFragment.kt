package com.martinszuc.phishing_emails_detection.ui.component.training

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlTrainingBinding
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
    private val trainingViewModel: TrainingViewModel by activityViewModels()

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
        val fab: FloatingActionButton = binding.fab
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
            val ivCheck = binding.ivCheck
            if (isFinished) {
                ivCheck.visibility = View.VISIBLE
                // Optionally, hide the ProgressBar when finished
                binding.progressBar.visibility = View.GONE
            } else {
                ivCheck.visibility = View.GONE
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
