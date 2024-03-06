package com.martinszuc.phishing_emails_detection.ui.component.machine_learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.martinszuc.phishing_emails_detection.databinding.FragmentMachineLearningBinding

/**
 * Authored by matoszuc@gmail.com
 */
class MachineLearningFragment : Fragment() {

    private var _binding: FragmentMachineLearningBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMachineLearningBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}