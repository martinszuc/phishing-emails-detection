package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailsImportBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import.adapter.EmailsImportAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailMinimalSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.user.AccountSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class EmailsImportFragment : Fragment() {
    private var _binding: FragmentEmailsImportBinding? = null
    private val emailsImportViewModel: EmailsImportViewModel by viewModels()
    private val emailMinimalSharedViewModel: EmailMinimalSharedViewModel by activityViewModels()
    private val accountSharedViewModel: AccountSharedViewModel by activityViewModels() // Inject UserAccountViewModel
    private lateinit var emailsImportAdapter: EmailsImportAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("EmailImportFragment", "onCreateView called")
        _binding = FragmentEmailsImportBinding.inflate(inflater, container, false)

        initUserAccount()
        initFloatingActionButton()
        initEmailsImport()
        initSearchView()

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

    private fun initFloatingActionButton() {
        val fab: FloatingActionButton = binding.fab

        // Set an observer on the selectedEmails LiveData
        emailsImportViewModel.selectedEmails.observe(viewLifecycleOwner) { emails ->
            if (emails.isNotEmpty()) {
                fab.show()
            } else {
                fab.hide()
            }
        }

        fab.setOnClickListener {
            emailsImportViewModel.importSelectedEmails()
            Toast.makeText(context, "Emails successfully saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initEmailsImport() {
        // Initialize RecyclerView
        val recyclerView: RecyclerView = binding.emailSelectionRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize EmailAdapter
        emailsImportAdapter = EmailsImportAdapter(emailsImportViewModel)
        recyclerView.adapter = emailsImportAdapter

        recyclerView.visibility = View.VISIBLE
        binding.searchView.visibility = View.VISIBLE

        observeEmailsFlow()

    }

    private fun observeEmailsFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    emailMinimalSharedViewModel.remoteEmailsFlow.collectLatest { pagingData ->
                        emailsImportAdapter.submitData(pagingData)
                    }
                }
            } catch (e: UserRecoverableAuthIOException) {
                // Start the activity for result using the intent from the exception
//                requestConsent.launch(e.intent)
            }
        }
    }

    private fun initUserAccount() {
        accountSharedViewModel.account.observe(viewLifecycleOwner) { account ->
            Log.d("EmailImportFragment", "Account: $account")
        }
    }

    private fun initSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // This method is called when the user submits the search string.
                query?.let {
                    emailMinimalSharedViewModel.searchRemoteEmails(it)
                }

                hideKeyboard()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // This method is called when the query text is changed by the user.
                if (newText.isNullOrEmpty()) {
                    // If the query text is empty, call getEmails to fetch all emails.
                    emailMinimalSharedViewModel.getRemoteEmails()
                }
                return true
            }
        })
    }

    private fun hideKeyboard() {
        val inputManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    // Show a loading spinner while a refresh operation is in progress
    private fun initLoadingSpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            emailsImportAdapter.loadStateFlow.collectLatest { loadStates ->
                binding.loadingSpinner.visibility =
                    if (loadStates.refresh is LoadState.Loading) View.VISIBLE else View.GONE
                Log.d("EmailImportFragment", "Load state changed: ${loadStates.refresh}")
            }
        }
    }
}



