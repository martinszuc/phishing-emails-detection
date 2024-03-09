package com.martinszuc.phishing_emails_detection.ui.component.data_picking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.martinszuc.phishing_emails_detection.databinding.FragmentMlDataPickingBinding
import com.martinszuc.phishing_emails_detection.ui.component.data_picking.adapter.DataPickingSelectionAdapter
import com.martinszuc.phishing_emails_detection.ui.component.machine_learning.MachineLearningParentSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.component.machine_learning.MachineLearningState
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
    private val machineLearningParentSharedViewModel: MachineLearningParentSharedViewModel by activityViewModels()
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
            onTrainingClicked = {
                machineLearningParentSharedViewModel.setState(MachineLearningState.TRAINING)
            },
            onRetrainingClicked = {
                machineLearningParentSharedViewModel.setState(MachineLearningState.RETRAINING)
            },
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
        val fab: FloatingActionButton = binding.fab

        // Set an observer on the selectedEmails LiveData
        dataPickingViewModel.selectedPackages.observe(viewLifecycleOwner) { selectedPackages ->
            if (selectedPackages.isNotEmpty()) {
                fab.show()
            } else {
                fab.hide()
            }
        }

        fab.setOnClickListener {
            // Set the state to DATA_PROCESSING to navigate to the Data Processing Fragment
            machineLearningParentSharedViewModel.setState(MachineLearningState.DATA_PROCESSING)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


