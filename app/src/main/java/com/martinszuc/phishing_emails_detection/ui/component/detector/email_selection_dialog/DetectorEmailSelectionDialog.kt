package com.martinszuc.phishing_emails_detection.ui.component.detector.email_selection_dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.martinszuc.phishing_emails_detection.databinding.DialogDetectorEmailSelectionBinding
import com.martinszuc.phishing_emails_detection.ui.component.detector.DetectorViewModel
import com.martinszuc.phishing_emails_detection.ui.component.detector.adapter.EmailSelectionDetectorItemListener
import com.martinszuc.phishing_emails_detection.ui.component.detector.adapter.EmailsSelectionDetectorAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailMinimalSharedViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Authored by matoszuc@gmail.com
 */
class DetectorEmailSelectionDialog : BottomSheetDialogFragment(), EmailSelectionDetectorItemListener {
    private var _binding: DialogDetectorEmailSelectionBinding? = null
    private val binding get() = _binding!!
    private val detectorViewModel: DetectorViewModel by activityViewModels()
    private val emailMinimalSharedViewModel: EmailMinimalSharedViewModel by activityViewModels()
    private lateinit var detectorAdapter: EmailsSelectionDetectorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogDetectorEmailSelectionBinding.inflate(inflater, container, false)
        detectorAdapter = EmailsSelectionDetectorAdapter(detectorViewModel, viewLifecycleOwner, this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView (adapter, layout manager, etc.)
        binding.emailList.adapter = detectorAdapter
        binding.emailList.layoutManager = LinearLayoutManager(context)

        // Observe the emailsFlow from the ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                emailMinimalSharedViewModel.emailsFlow.collectLatest { pagingData ->
                    Log.d("EmailListBottomSheetFragment", "New PagingData received")
                    detectorAdapter.submitData(pagingData)
                }
            }
        }

        // Set up your FloatingActionButton click listener
        binding.fabSubmit.setOnClickListener {
            detectorViewModel.classifySelectedMinimalEmail()
            dismiss()
        }
    }

    override fun onEmailClicked(emailId: String) {
        detectorViewModel.toggleEmailSelected(emailId)
        detectorViewModel.classifySelectedMinimalEmail()
        dismiss()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
