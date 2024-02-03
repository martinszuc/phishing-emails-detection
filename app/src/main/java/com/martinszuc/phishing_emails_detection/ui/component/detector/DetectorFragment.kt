package com.martinszuc.phishing_emails_detection.ui.component.detector

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.FragmentDetectorBinding
import com.martinszuc.phishing_emails_detection.ui.component.detector.adapter.EmailsSelectionDetectorAdapter
import com.martinszuc.phishing_emails_detection.ui.component.detector.email_selection_dialog.DetectorEmailSelectionDialog
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailMinimalSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val BODY_SIZE = 195

@AndroidEntryPoint
class DetectorFragment : Fragment() {               // TODO this fragment lags UI when loading
    private var _binding: FragmentDetectorBinding? = null
    private val binding get() = _binding!!
    private val detectorViewModel: DetectorViewModel by activityViewModels()
    private val emailMinimalSharedViewModel: EmailMinimalSharedViewModel by activityViewModels()
    private lateinit var emailsSelectionDetectorAdapter: EmailsSelectionDetectorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d("DetectorFragment", "onCreateView")
        _binding = FragmentDetectorBinding.inflate(inflater, container, false)

        // Initialize detectorAdapter
        emailsSelectionDetectorAdapter = EmailsSelectionDetectorAdapter(detectorViewModel, viewLifecycleOwner)
        observeEmailsFlow()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeIsFinishedLoading()
        observeIsLoading()
        observeResult()
        setupDetectButton()
        setupSelectedEmailWindow(view)

        binding.emailSelectionButton.setOnClickListener {
            openEmailSelectionBottomSheet()
        }


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


    private fun openEmailSelectionBottomSheet() {
        val bottomSheetFragment = DetectorEmailSelectionDialog()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    private fun setupDetectButton() {
        binding.detectButton.setOnClickListener {
            detectorViewModel.classifySelectedMinimalEmail()
        }
    }

    private fun observeResult() {
        detectorViewModel.classificationResult.observe(viewLifecycleOwner) { isPhishing ->
            // Determine the text to display based on the classification result
            val resultText = if (isPhishing) "Phishing" else "Safe"
            binding.textResult.text = resultText
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
                emailMinimalSharedViewModel.emailsFlow.collectLatest { pagingData ->
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
