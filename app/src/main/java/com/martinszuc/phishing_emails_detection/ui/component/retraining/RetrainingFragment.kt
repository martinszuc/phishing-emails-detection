package com.martinszuc.phishing_emails_detection.ui.component.retraining

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlRetrainingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RetrainingFragment : Fragment() {

    private var _binding: FragmentMlRetrainingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMlRetrainingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
