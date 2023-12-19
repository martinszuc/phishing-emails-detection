package com.martinszuc.phishing_emails_detection.ui.component.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.martinszuc.phishing_emails_detection.databinding.FragmentSettingsBinding

/**
 * Authored by matoszuc@gmail.com
 */
class SettingsFragment : Fragment() {
// TODO thisssss
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}