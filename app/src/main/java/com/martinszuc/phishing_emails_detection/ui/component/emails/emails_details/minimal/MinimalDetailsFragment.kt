package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.minimal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.databinding.FragmentDetailsMinimalBinding
import com.martinszuc.phishing_emails_detection.utils.DateUtils

class MinimalDetailsFragment : Fragment() {

    private var _binding: FragmentDetailsMinimalBinding? = null
    private val binding get() = _binding!!

    private val email by lazy {
        arguments?.getParcelable<EmailMinimal>("email")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsMinimalBinding.inflate(inflater, container, false)

        email?.let { email ->
            with(binding) {
                emailSenderTextView.text = "Sender: ${email.sender}"
                emailSubjectTextView.text = "Subject: ${email.subject}"
                emailBodyTextView.text = "Body:\n${email.body}"
                emailTimestampTextView.text = "Timestamp: ${DateUtils.formatTimestamp(email.timestamp)}"
                emailIsPhishingChip.text = "Phishing Status: ${if (email.isPhishing == true) "Suspicious" else "Safe"}"
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(email: EmailMinimal): MinimalDetailsFragment {
            val fragment = MinimalDetailsFragment()
            val bundle = Bundle()
            bundle.putParcelable("email", email)
            fragment.arguments = bundle
            return fragment
        }
    }
}
