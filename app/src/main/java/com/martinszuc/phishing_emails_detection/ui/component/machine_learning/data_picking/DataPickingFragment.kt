package com.martinszuc.phishing_emails_detection.ui.component.machine_learning.data_picking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlDataPickingBinding
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseFragment
import com.martinszuc.phishing_emails_detection.ui.component.machine_learning.data_picking.adapter.DataPickingSelectionAdapter
import com.martinszuc.phishing_emails_detection.ui.component.machine_learning.data_processing.DataProcessingBottomSheetFragment
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailPackageSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * @author matoszuc@gmail.com
 */

@AndroidEntryPoint
class DataPickingFragment : AbstractBaseFragment() {

    private var _binding: FragmentMlDataPickingBinding? = null
    private val binding get() = _binding!!
    private val emailPackageSharedViewModel: EmailPackageSharedViewModel by activityViewModels()
    private val dataPickingViewModel: DataPickingViewModel by activityViewModels()

    private lateinit var emailPackageSelectionAdapter: DataPickingSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMlDataPickingBinding.inflate(inflater, container, false)
        setupRecyclerView()
        initFloatingActionButton()
        return binding.root
    }

    private fun setupRecyclerView() {
        emailPackageSelectionAdapter = DataPickingSelectionAdapter(
            onPackageSelected = { emailPackage, isChecked ->
                // Assuming emailPackage is an EmailPackageMetadata object
                dataPickingViewModel.togglePackageSelected(emailPackage)
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

    private fun initFloatingActionButton() {
        val fab: ExtendedFloatingActionButton = binding.fab

        // Set an observer on the selectedEmails LiveData
        dataPickingViewModel.selectedPackages.observe(viewLifecycleOwner) { selectedPackages ->
            if (selectedPackages.isNotEmpty()) {
                fab.show()
            } else {
                fab.hide()
            }
        }

        fab.setOnClickListener {
            showDataProcessingBottomSheet()
        }
    }

    private fun showDataProcessingBottomSheet() {
        val bottomSheetFragment = DataProcessingBottomSheetFragment()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvEmailPackages.adapter = null
        // Clear selected packages
        dataPickingViewModel.clearSelectedPackages()
        _binding = null
    }
}


