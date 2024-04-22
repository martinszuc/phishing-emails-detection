package com.martinszuc.phishing_emails_detection.ui.component.learning

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.martinszuc.phishing_emails_detection.databinding.FragmentLearningBinding
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseFragment
import com.martinszuc.phishing_emails_detection.utils.Constants

/**
 * Authored by matoszuc@gmail.com
 */

class LearningFragment : AbstractBaseFragment() {   // TODO maybe change this to model info and evaluation on a testing dataset from internal

    private var _binding: FragmentLearningBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("LearningFragment", "onCreateView() called")
        _binding = FragmentLearningBinding.inflate(inflater, container, false)

        binding.phishingQuizViewButton.setOnClickListener{
            openWeb(Constants.PHISHING_QUIZ_LINK)
        }

        binding.openInfoButton.setOnClickListener{
            openWeb(Constants.PHISHING_INFO_LINK)
        }

        return binding.root
    }

    private fun openWeb(urlKey: String) {
        Log.d("LearningFragment", "openWeb() called with URL_KEY: $urlKey")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlKey))
        intent.setPackage("com.android.chrome")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Chrome is not installed
            intent.setPackage(null)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(Intent.createChooser(intent, "Choose browser"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("LearningFragment", "onDestroyView() called")
        _binding = null
    }
}
