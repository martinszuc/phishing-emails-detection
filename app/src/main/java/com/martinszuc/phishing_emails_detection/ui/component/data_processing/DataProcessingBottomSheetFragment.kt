package com.martinszuc.phishing_emails_detection.ui.component.data_processing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlDataProcessingBinding
import com.martinszuc.phishing_emails_detection.ui.component.data_picking.DataPickingViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.ProcessedPackageSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class DataProcessingBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentMlDataProcessingBinding? = null
    private val binding get() = _binding!!

    private val dataProcessingViewModel: DataProcessingViewModel by viewModels()
    private val dataPickingViewModel: DataPickingViewModel by activityViewModels()
    private val processedPackageSharedViewModel: ProcessedPackageSharedViewModel by activityViewModels()

    private var processingStarted = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMlDataProcessingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

                processedPackageSharedViewModel.refreshAndLoadProcessedPackages()

                processingStarted = false // Reset the flag after handling
                binding.btnProcessEmails.isEnabled = false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { bs ->
                val behavior = BottomSheetBehavior.from(bs)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                // Set max height to 80% of the screen
                val maxHeight = (resources.displayMetrics.heightPixels * 0.8).toInt()
                behavior.peekHeight = maxHeight

                // Force the bottom sheet to use a fixed height
                val layoutParams = bs.layoutParams
                layoutParams.height = maxHeight
                bs.layoutParams = layoutParams
            }
        }
    }

    private fun processPackages(packages: Set<EmailPackageMetadata>) {
        dataProcessingViewModel.processEmailPackages(packages)
    }


    override fun onDestroyView() {
        lifecycleScope.cancel()  // Cancel all coroutines started by this fragment's scope
        super.onDestroyView()
        _binding = null
    }
}