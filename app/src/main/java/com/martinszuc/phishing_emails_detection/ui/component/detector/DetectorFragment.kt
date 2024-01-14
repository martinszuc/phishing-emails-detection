package com.martinszuc.phishing_emails_detection.ui.component.detector

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.martinszuc.phishing_emails_detection.databinding.FragmentDetectorBinding
import com.martinszuc.phishing_emails_detection.ui.component.detector.adapter.EmailsSelectionDetectorAdapter
import com.martinszuc.phishing_emails_detection.ui.component.detector.email_selection_dialog.DetectorEmailSelectionDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetectorFragment : Fragment() {
    private var _binding: FragmentDetectorBinding? = null
    private val binding get() = _binding!!
    private val detectorViewModel: DetectorViewModel by activityViewModels()
    private lateinit var emailsSelectionDetectorAdapter: EmailsSelectionDetectorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d("DetectorFragment", "onCreateView")
        _binding = FragmentDetectorBinding.inflate(inflater, container, false)

        // Initialize detectorAdapter
        emailsSelectionDetectorAdapter = EmailsSelectionDetectorAdapter(detectorViewModel)
        observeEmailsFlow()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeIsFinishedLoading()
        observeIsLoading()
        observeResult()
        setupDetectButton()

        binding.emailSelectionButton.setOnClickListener {
            openEmailSelectionBottomSheet()
        }

    }

    private fun openEmailSelectionBottomSheet() {
        val bottomSheetFragment = DetectorEmailSelectionDialog()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    private fun setupDetectButton() {
        binding.detectButton.setOnClickListener {
            detectorViewModel.classifySelectedEmail()
        }
    }

    private fun observeResult() {
        detectorViewModel.classificationResult.observe(viewLifecycleOwner) { result ->
            // Display the classification result in the TextView
            val percentageString = "%.2f%%".format(result * 100)
            binding.textResult.text = percentageString
        }
    }

    private fun observeIsLoading() {
        detectorViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.loadingBar.visibility = View.VISIBLE
                binding.textResult.visibility = View.GONE
            } else {
                binding.loadingBar.visibility = View.GONE
                binding.textResult.visibility = View.VISIBLE
            }
        }
    }

    private fun observeIsFinishedLoading() {
        detectorViewModel.isFinished.observe(viewLifecycleOwner) { isFinished ->
            if (isFinished) {
                binding.textResult.visibility = View.VISIBLE
            }
        }
    }

    private fun observeEmailsFlow() {
        Log.d("DetectorFragment", "observeEmailsFlow")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                detectorViewModel.emailsFlow.collectLatest { pagingData ->
                    Log.d("DetectorFragment", "New PagingData received")
                    emailsSelectionDetectorAdapter.submitData(pagingData)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("DetectorFragment", "onDestroyView")
        _binding = null
    }
}
