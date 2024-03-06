package com.martinszuc.phishing_emails_detection.ui.component.data_processing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlDataProcessingBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class DataProcessingFragment : Fragment() {

    private var _binding: FragmentMlDataProcessingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMlDataProcessingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
