package com.martinszuc.phishing_emails_detection.ui.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.martinszuc.phishing_emails_detection.data.AppDatabase
import com.martinszuc.phishing_emails_detection.data.repository.EmailRepository
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailImportBinding
import com.martinszuc.phishing_emails_detection.ui.adapter.EmailAdapter
import com.martinszuc.phishing_emails_detection.ui.viewmodel.EmailViewModel
import com.martinszuc.phishing_emails_detection.ui.viewmodel.SharedViewModel
import com.martinszuc.phishing_emails_detection.ui.viewmodel.factory.EmailViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// TODO make this fragment into MyEmails fragment and add fragment pages for database, gmail, detection pages
class EmailImportFragment : Fragment() {

    private var _binding: FragmentEmailImportBinding? = null
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var viewModel: EmailViewModel
    private lateinit var emailAdapter: EmailAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("EmailImportFragment", "onCreateView called")
        _binding = FragmentEmailImportBinding.inflate(inflater, container, false)

        // Get an instance of SharedViewModel from the activity
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        val account: GoogleSignInAccount? = sharedViewModel.account.value
        if (account != null) {
            Log.d("EmailImportFragment", "Account is not null")

            val database = AppDatabase.getDatabase(requireContext())
            val emailRepository = EmailRepository(database, requireContext(), account)
            val viewModelFactory = EmailViewModelFactory(emailRepository)

            viewModel = ViewModelProvider(this, viewModelFactory).get(EmailViewModel::class.java)

            initEmailsImport()
            initSearchView()
        } else {
            Log.d("EmailImportFragment", "Account is null")
            // TODO handle the case where account is null
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe load state changes
        initLoadingSpinner()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("EmailImportFragment", "onDestroyView called")
        _binding = null
    }


    private fun initEmailsImport() {
        // Initialize RecyclerView
        val recyclerView: RecyclerView = binding.emailSelectionRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize EmailAdapter
        emailAdapter = EmailAdapter(viewModel)
        recyclerView.adapter = emailAdapter

        // Initialize Import Emails Button
        val importEmailsButton: Button = binding.importEmailsButton
        importEmailsButton.setOnClickListener {
            importEmailsButton.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            // Start observing the emails once button is pressed
            observeEmailsFlow()
        }
    }

    private fun observeEmailsFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.emailsFlow.collectLatest { pagingData ->
                    emailAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun initSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // This method is called when the user submits the search string.
                query?.let {
                    // Call searchEmails in the ViewModel with the search query.
                    viewModel.searchEmails(it)
                }

                // Hide the keyboard.
                val inputManager =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // This method is called when the query text is changed by the user.
                if (newText.isNullOrEmpty()) {
                    // If the query text is empty, call getEmails to fetch all emails.
                    viewModel.getEmails()
                }
                return true
            }
        })
    }

    private fun initLoadingSpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            emailAdapter.loadStateFlow.collectLatest { loadStates ->
                // Show a loading spinner while a refresh operation is in progress
                binding.loadingSpinner.visibility =
                    if (loadStates.refresh is LoadState.Loading) View.VISIBLE else View.GONE
                Log.d("EmailImportFragment", "Load state changed: ${loadStates.refresh}")
            }
        }
    }
}



