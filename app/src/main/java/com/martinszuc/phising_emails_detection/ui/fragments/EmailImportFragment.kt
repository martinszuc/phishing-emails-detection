package com.martinszuc.phising_emails_detection.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.martinszuc.phising_emails_detection.data.EmailRepository
import com.martinszuc.phising_emails_detection.databinding.FragmentEmailImportBinding
import com.martinszuc.phising_emails_detection.ui.adapters.EmailAdapter
import com.martinszuc.phising_emails_detection.ui.viewmodels.EmailViewModel
import com.martinszuc.phising_emails_detection.ui.viewmodels.EmailViewModelFactory
import com.martinszuc.phising_emails_detection.ui.viewmodels.SharedViewModel

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

            // Create and set an instance of your adapter
            val emailAdapter = EmailAdapter(emptyList(), viewModel)
            recyclerView.adapter = emailAdapter

            // Observe the emails LiveData
            viewModel.emails.observe(viewLifecycleOwner) { emails ->
                emailAdapter.updateEmails(emails)
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
