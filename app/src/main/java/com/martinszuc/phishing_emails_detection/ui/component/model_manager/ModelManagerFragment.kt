package com.martinszuc.phishing_emails_detection.ui.component.model_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.martinszuc.phishing_emails_detection.databinding.FragmentModelManagerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ModelManagerFragment : Fragment() {

    private var _binding: FragmentModelManagerBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentModelManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Include other fragment methods or logic here as needed
}
