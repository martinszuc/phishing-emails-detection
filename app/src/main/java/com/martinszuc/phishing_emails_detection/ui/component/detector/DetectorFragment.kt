package com.martinszuc.phishing_emails_detection.ui.component.detector

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.databinding.FragmentDetectorBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import.EmailsImportViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetectorFragment : Fragment() {
    private var _binding: FragmentDetectorBinding? = null
    private val binding get() = _binding!!
    private val detectorViewModel: DetectorViewModel by viewModels()
    private val emailsImportViewModel: EmailsImportViewModel by activityViewModels()
    private lateinit var detectorAdapter: DetectorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d("DetectorFragment", "onCreateView")
        _binding = FragmentDetectorBinding.inflate(inflater, container, false)

        initEmailsSaved()
        observeEmailsFlow()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe selectedEmailId
        detectorViewModel.selectedEmailId.observe(viewLifecycleOwner) { emailId ->
            // TODO
        }

        // Observe isFinished
        detectorViewModel.isFinished.observe(viewLifecycleOwner) { isFinished ->
            if (isFinished) {
             binding.textResult.visibility = View.VISIBLE
            }
        }

        // Observe isLoading
        detectorViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                // Show ProgressBar and hide TextView
                binding.loadingBar.visibility = View.VISIBLE
                binding.textResult.visibility = View.GONE
            } else {
                // Hide ProgressBar and show TextView
                binding.loadingBar.visibility = View.GONE
                binding.textResult.visibility = View.VISIBLE
            }
        }

        // Observe classificationResult
        detectorViewModel.classificationResult.observe(viewLifecycleOwner) { result ->
            // Display the classification result in the TextView
            binding.textResult.text = result.toString()
        }

        binding.detectButton.setOnClickListener {
            detectorViewModel.classifySelectedEmail()
        }
    }

    private fun initEmailsSaved() {
        Log.d("DetectorFragment", "initEmailsSaved")
        val recyclerView: RecyclerView = binding.emailList
        recyclerView.layoutManager = LinearLayoutManager(context)

        detectorAdapter = DetectorAdapter(detectorViewModel)
        recyclerView.adapter = detectorAdapter
        recyclerView.setHasFixedSize(true)
    }

    private fun observeEmailsFlow() {
        Log.d("DetectorFragment", "observeEmailsFlow")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                detectorViewModel.emailsFlow.collectLatest { pagingData ->
                    Log.d("DetectorFragment", "New PagingData received")
                    detectorAdapter.submitData(pagingData)
//                    binding.emailList.layoutManager?.scrollToPosition(0)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("DetectorFragment", "onDestroyView")
        _binding = null
    }
}
