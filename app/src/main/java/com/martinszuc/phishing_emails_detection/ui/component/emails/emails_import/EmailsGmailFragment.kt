package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.SearchView
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
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailsImportBinding
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import.adapter.EmailsImportAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailMinimalSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.user.AccountSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val logTag = "EmailsImportFragment"

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class EmailsGmailFragment :
    AbstractBaseFragment() {                       // TODO better loading screen when batch downloading
    private var _binding: FragmentEmailsImportBinding? = null
    private val emailsGmailViewModel: EmailsGmailViewModel by viewModels()
    private val emailMinimalSharedViewModel: EmailMinimalSharedViewModel by activityViewModels()
    private val accountSharedViewModel: AccountSharedViewModel by activityViewModels() // Inject UserAccountViewModel
    private lateinit var emailsImportAdapter: EmailsImportAdapter

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(logTag, "onCreateView called")
        _binding = FragmentEmailsImportBinding.inflate(inflater, container, false)

        initUserAccount()
        initImportFAB()
        initEmailsImport()
        initSearchView()
        initObserveSelectedEmails()
        initFetchEmailsButton()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        initLoadingSpinner()
        setupEmailsLoadedObserver()
    }

    private fun setupEmailsLoadedObserver() {
        emailMinimalSharedViewModel.emailsLoaded.observe(viewLifecycleOwner) { emailsLoaded ->
            if (emailsLoaded) {
                // Emails have been loaded
                binding.searchViewContainer.visibility = View.VISIBLE
                binding.btnFetchEmails.visibility = View.GONE
            } else {
                // No emails loaded yet
                binding.searchViewContainer.visibility = View.GONE
                binding.btnFetchEmails.visibility = View.VISIBLE
            }
        }
    }

    private fun initObserveSelectedEmails() {
        // Observe selected emails LiveData to update UI accordingly
        emailsGmailViewModel.selectedEmails.observe(viewLifecycleOwner) {
            // Notify the adapter that the selection state has changed
            emailsImportAdapter.notifyDataSetChanged() // This triggers a UI refresh
        }
    }

    private fun initFetchEmailsButton() {
        binding.btnFetchEmails.setOnClickListener {
            // Trigger email fetching here
            emailMinimalSharedViewModel.getRemoteEmails()
            binding.searchViewContainer.visibility = View.VISIBLE
            it.visibility = View.GONE // Hide the button
        }
    }

    private fun showBatchImportDialog(currentQuery: String) {
        val input = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Enter number of emails to import"
        }
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Batch Import Emails")
            setView(input)
            setPositiveButton("Ok") { _, _ ->
                val count = input.text.toString().toIntOrNull()
                if (count != null) {
                    emailsGmailViewModel.fetchAndSaveEmailsBasedOnFilterAndLimit(
                        currentQuery,
                        count
                    )
                } else {
                    showToast("Please enter a valid number")
                }
            }
            setNegativeButton(getString(R.string.cancel_big), null)
            show()
        }
    }

    private fun initEmailsImport() {
        // Initialize RecyclerView
        val recyclerView: RecyclerView = binding.emailSelectionRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize EmailAdapter
        emailsImportAdapter = EmailsImportAdapter(emailsGmailViewModel)
        recyclerView.adapter = emailsImportAdapter

        recyclerView.visibility = View.VISIBLE
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
            Log.d(logTag, "Account: $account")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.emailSelectionRecyclerView.adapter = null
        lifecycleScope.cancel()
        Log.d(logTag, "onDestroyView called")
        _binding = null
    }

    private fun initImportFAB() {
        val fab: FloatingActionButton = binding.fab

        // Set an observer on the selectedEmails LiveData
        emailsGmailViewModel.selectedEmails.observe(viewLifecycleOwner) { emails ->
            if (emails.isNotEmpty()) {
                fab.show()
            } else {
                fab.hide()
            }
        }

        fab.setOnClickListener {
            emailsGmailViewModel.importSelectedEmails()
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

        // Initialize batch download button listener
        binding.btnBatchImport.setOnClickListener {
            val currentQuery = binding.searchView.query.toString()
            showBatchImportDialog(currentQuery)
        }
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
                Log.d(logTag, "Load state changed: ${loadStates.refresh}")
            }
        }
    }

    private fun setupObservers() {
        emailsGmailViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        emailsGmailViewModel.isFinished.observe(viewLifecycleOwner) { isFinished ->
            if (isFinished) {
                showImportFinishedToast(success = true)
                binding.loadingOverlay.visibility = View.GONE  // Hide the overlay when finished
            }
        }

        emailsGmailViewModel.operationFailed.observe(viewLifecycleOwner) { isFailed ->
            if (isFailed) {
                showImportFinishedToast(success = false)
                binding.loadingOverlay.visibility = View.GONE  // Hide the overlay on failure
            }
        }

        emailsGmailViewModel.totalCount.observe(viewLifecycleOwner) { total ->
            if (total > 0) {
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.progressBar.max = total
                binding.progressText.text = "0 / $total"
            }
        }

        emailsGmailViewModel.progress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress
            binding.progressText.text = "$progress / ${binding.progressBar.max}"
        }
    }


    private fun showImportFinishedToast(success: Boolean) {
        val message = if (success) "Import finished successfully." else "Import failed."
        showToast(message)
    }
}



