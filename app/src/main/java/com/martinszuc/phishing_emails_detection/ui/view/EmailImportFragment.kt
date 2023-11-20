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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailImportBinding
import com.martinszuc.phishing_emails_detection.ui.adapter.EmailAdapter
import com.martinszuc.phishing_emails_detection.ui.viewmodel.EmailViewModel
import com.martinszuc.phishing_emails_detection.ui.viewmodel.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// TODO make this fragment into MyEmails fragment and add fragment pages for database, gmail, detection pages
@AndroidEntryPoint
class EmailImportFragment : Fragment() {
    private var _binding: FragmentEmailImportBinding? = null
    private val emailViewModel: EmailViewModel by activityViewModels()
    private val userAccountViewModel: UserAccountViewModel by activityViewModels() // Inject UserAccountViewModel
    private lateinit var emailAdapter: EmailAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("EmailImportFragment", "onCreateView called")
        _binding = FragmentEmailImportBinding.inflate(inflater, container, false)

        observeAccount()

        initEmailsImport()
        initSearchView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe load state changes
        initLoadingSpinner()

        // Observe account changes
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
        emailAdapter = EmailAdapter(emailViewModel)
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
                emailViewModel.emailsFlow.collectLatest { pagingData ->
                    emailAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun observeAccount() {
        userAccountViewModel.account.observe(viewLifecycleOwner) { account ->
            Log.d("EmailImportFragment", "Account: $account")
            // Use the account to initialize your Gmail API service here
        }
    }

    private fun initSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // This method is called when the user submits the search string.
                query?.let {
                    // Call searchEmails in the ViewModel with the search query.
                    emailViewModel.searchEmails(it)
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
                    emailViewModel.getEmails()
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



