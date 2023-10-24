package com.martinszuc.phising_emails_detection.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.martinszuc.phising_emails_detection.R
import com.martinszuc.phising_emails_detection.databinding.FragmentDashboardBinding

// TODO there is a back button from this to the login screen
// TODO change labels everywhere
// TODO consider what to do with upper actionbar and its menu
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.importEmailButton.setOnClickListener {
            findNavController().navigate(R.id.action_DashboardFragment_to_EmailImportFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
