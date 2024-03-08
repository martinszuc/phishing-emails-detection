package com.martinszuc.phishing_emails_detection.ui.component.data_picking

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlDataPickingBinding
import com.martinszuc.phishing_emails_detection.ui.component.data_picking.adapter.DataPickingSelectionAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.EmailPackageSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class DataPickingFragment : Fragment() {

    private var _binding: FragmentMlDataPickingBinding? = null
    private val binding get() = _binding!!
    private val emailPackageSharedViewModel: EmailPackageSharedViewModel by activityViewModels()
    private val dataPickingViewModel: DataPickingViewModel by viewModels()

    private lateinit var emailPackageSelectionAdapter: DataPickingSelectionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMlDataPickingBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        emailPackageSelectionAdapter = DataPickingSelectionAdapter(
            onAddClicked = {
                // Your logic for adding a new package
            },
            onPackageSelected = { emailPackage, isChecked ->
                val packageName = emailPackage.packageName
                dataPickingViewModel.togglePackageSelected(packageName)
            }
        )
        binding.rvEmailPackages.layoutManager = LinearLayoutManager(context)
        binding.rvEmailPackages.adapter = emailPackageSelectionAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeEmailPackages()
        emailPackageSharedViewModel.loadEmailPackages()
    }

    private fun observeEmailPackages() {
        emailPackageSharedViewModel.emailPackages.observe(viewLifecycleOwner) { packages ->
            emailPackageSelectionAdapter.setItems(packages)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


