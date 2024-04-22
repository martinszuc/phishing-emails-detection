package com.martinszuc.phishing_emails_detection.ui.component.detector

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.model_manager.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.databinding.FragmentDetectorBinding
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseFragment
import com.martinszuc.phishing_emails_detection.ui.component.detector.email_selection_dialog.DetectorEmailSelectionDialog
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.ModelManagerSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Authored by matoszuc@gmail.com
 */

private const val BODY_SIZE = 195

@AndroidEntryPoint
class DetectorFragment : AbstractBaseFragment() {
    private var _binding: FragmentDetectorBinding? = null
    private val binding get() = _binding!!
    private val detectorViewModel: DetectorViewModel by activityViewModels()
    private val modelManagerSharedViewModel: ModelManagerSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d("DetectorFragment", "onCreateView")
        _binding = FragmentDetectorBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeIsLoading()
        setupDetectButton()
        setupSelectedEmailWindow(view)
        observeModels()
        binding.frameSelectedEmail.setOnClickListener {
            openEmailSelectionBottomSheet()
        }
        observeIsFinishedLoading()
    }

    private fun observeIsFinishedLoading() {
        detectorViewModel.isFinished.observe(viewLifecycleOwner) { isFinished ->
            if (isFinished) {
                detectorViewModel.classificationResult.value?.let { isPhishing ->
                    showPredictionResultDialog(isPhishing)
                }
            }
        }
    }

    private fun showPredictionResultDialog(isPhishing: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_prediction_finished, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false) // Prevents dismissing by tapping outside
            .create()

        val imageView: ImageView = dialogView.findViewById(R.id.ivPredictionResult)
        val textView: TextView = dialogView.findViewById(R.id.tvPredictionResult)
        val dismissButton: Button = dialogView.findViewById(R.id.btnDismiss)

        // Update dialog content based on prediction
        if (isPhishing) {
            imageView.setImageResource(R.drawable.ic_phishing) // Replace with your phishing icon drawable
            textView.text = getString(R.string.phishing_warning) // Assuming you have a string resource
        } else {
            imageView.setImageResource(R.drawable.ic_safe) // Replace with your safe icon drawable
            textView.text = getString(R.string.safe_email) // Assuming you have a string resource
        }

        dismissButton.setOnClickListener {
            detectorViewModel.clearIsFinished()
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun setupSelectedEmailWindow(view: View) {
        // Find the views in the included layout using findViewById
        val subjectValue = view.findViewById<TextView>(R.id.subject_value)
        val senderValue = view.findViewById<TextView>(R.id.sender_value)
        val bodyText = view.findViewById<TextView>(R.id.body_text)

        // Observe changes in selectedEmail and update the layout when it changes
        detectorViewModel.selectedEmailId.observe(viewLifecycleOwner) { emailId ->
            if (emailId != null) {
                // Use lifecycleScope to launch a coroutine
                viewLifecycleOwner.lifecycleScope.launch {
                    // Update the layout with the selected email's details
                    val email = detectorViewModel.getMinimalEmailById(emailId)

                    // Update the views with the email details
                    email?.let {
                        subjectValue.text = email.subject
                        senderValue.text = email.sender

                        // Shorten the email body if it's longer than 100 characters
                        val body = if (email.body.length > BODY_SIZE) {
                            email.body.substring(0, BODY_SIZE) + "..."
                        } else {
                            email.body
                        }

                        bodyText.text = body
                    }
                }
            }
        }
    }

    private fun observeModels() {
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
                    getString(R.string.select_from_your_models_spinner),
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
                        detectorViewModel.toggleSelectedModel(selectedModel)
                        Toast.makeText(
                            requireContext(),
                            "Selected: ${selectedModel.modelName}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Optional: Handle the case where nothing is selected
                }
            }

        // Initially set the spinner to show the default item
        binding.spinnerModelSelector.setSelection(0)
    }


    private fun openEmailSelectionBottomSheet() {
        val bottomSheetFragment = DetectorEmailSelectionDialog()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    private fun setupDetectButton() {
        binding.detectButton.setOnClickListener {
            detectorViewModel.classifySelectedMinimalEmail()
        }
    }

    private fun observeIsLoading() {
        detectorViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingBar.visibility = View.VISIBLE
            } else {
                binding.loadingBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        detectorViewModel.clearIsFinished()
        detectorViewModel.clearSelectedModel()
        detectorViewModel.clearSelectedEmail()
        Log.d("DetectorFragment", "onDestroyView")
        _binding = null
    }
}
