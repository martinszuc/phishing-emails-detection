package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detector

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailPackageManagerBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detector.adapter.EmailPackageAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailPackageSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Authored by matoszuc@gmail.com
 */
@AndroidEntryPoint
class EmailsPackageManagerFragment : Fragment() {
    private var _binding: FragmentEmailPackageManagerBinding? = null
    private val binding get() = _binding!!
    private lateinit var emailPackageAdapter: EmailPackageAdapter
    private val emailPackageManagerViewModel: EmailPackageManagerViewModel by viewModels()
    private val emailPackageSharedViewModel: EmailPackageSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailPackageManagerBinding.inflate(inflater, container, false)
        setupRecyclerView()
        observeViewModel()
        initFloatingActionButton()
        return binding.root
    }

    private fun setupRecyclerView() {
        emailPackageAdapter = EmailPackageAdapter(emptyList(), onDeleteClicked = { fileName ->
            emailPackageManagerViewModel.deleteEmailPackage(fileName)
            emailPackageSharedViewModel.loadEmailPackages()
        })
        binding.rvEmailPackages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = emailPackageAdapter
        }
    }

    private fun observeViewModel() {
        emailPackageSharedViewModel.emailPackages.observe(viewLifecycleOwner) { packages ->
            Log.d("EmailsPackageManager", "Packages received: ${packages.size}")
            emailPackageAdapter.setItems(packages)
        }
    }

    private fun initFloatingActionButton() {
        val fab: FloatingActionButton = binding.fab
        fab.show()
        fab.setOnClickListener {
            emailPackageSharedViewModel.loadEmailPackages()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
