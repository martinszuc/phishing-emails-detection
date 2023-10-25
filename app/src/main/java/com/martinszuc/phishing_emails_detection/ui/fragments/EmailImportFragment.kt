package com.martinszuc.phishing_emails_detection.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.martinszuc.phishing_emails_detection.data.EmailRepository
import com.martinszuc.phishing_emails_detection.data.models.Email
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailImportBinding
import com.martinszuc.phishing_emails_detection.ui.adapters.EmailAdapter
import com.martinszuc.phishing_emails_detection.ui.viewmodels.EmailViewModel
import com.martinszuc.phishing_emails_detection.ui.viewmodels.EmailViewModelFactory
import com.martinszuc.phishing_emails_detection.ui.viewmodels.SharedViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EmailImportFragment : Fragment() {

    private var _binding: FragmentEmailImportBinding? = null
    private lateinit var sharedViewModel: SharedViewModel // Change this line

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailImportBinding.inflate(inflater, container, false)

        // Get an instance of SharedViewModel from the activity
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        val account: GoogleSignInAccount? = sharedViewModel.account.value
        if (account != null) {
            // Create an EmailRepository instance
            val emailRepository = EmailRepository(requireContext(), account)

            // Initialize ViewModel
            val viewModelFactory = EmailViewModelFactory(emailRepository)
            val viewModel: EmailViewModel by viewModels { viewModelFactory }

            // Initialize RecyclerView
            val recyclerView: RecyclerView = binding.emailSelectionRecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context)

            val emailAdapter = EmailAdapter(viewModel)
            recyclerView.adapter = emailAdapter

            // Collect the latest paging data and submit it to the adapter
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.emails.collectLatest { pagingData: PagingData<Email> ->
                    emailAdapter.submitData(pagingData)
                }
            }

        } else {
            // TODO handle the case where account is null
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
