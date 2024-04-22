package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.minimal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.databinding.FragmentDetailsMinimalBinding
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseFragment
import com.martinszuc.phishing_emails_detection.utils.StringUtils

/**
 * @author matoszuc@gmail.com
 */

class MinimalDetailsFragment : AbstractBaseFragment() {

    private var _binding: FragmentDetailsMinimalBinding? = null
    private val binding get() = _binding!!

    // Lazy initialization of email using safe call and Parcelable
    private val email: EmailMinimal? by lazy {
        arguments?.getParcelable("email")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailsMinimalBinding.inflate(inflater, container, false)

        binding.emailIsPhishingChip.visibility = View.GONE


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    // Initialize UI components
    private fun initUI() {
        startArrowIndicatorAnimation()
        populateEmailDetails()
    }

    // Start the arrow indicator animation
    private fun startArrowIndicatorAnimation() {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.arrow_fade_animation)
        binding.swipeRightIndicator.apply {
            startAnimation(animation)
        }
    }

    // Populate the UI with email details
    private fun populateEmailDetails() {
        email?.let {
            with(binding) {
                emailSenderTextView.text = getString(R.string.sender_details_minimal, it.sender)
                emailSubjectTextView.text = getString(R.string.subject_details_minimal, it.subject)
                emailBodyTextView.text = getString(R.string.body_details_minimal, it.body)
                emailTimestampTextView.text = getString(R.string.timestamp_details_minimal, StringUtils.formatTimestamp(it.timestamp))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Restart the animation when the fragment resumes
        startArrowIndicatorAnimation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(email: EmailMinimal): MinimalDetailsFragment =
            MinimalDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("email", email)
                }
            }
    }
}
