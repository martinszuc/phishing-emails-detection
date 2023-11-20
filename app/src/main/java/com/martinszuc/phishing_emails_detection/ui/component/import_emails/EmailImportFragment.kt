package com.martinszuc.phishing_emails_detection.ui.component.import_emails

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
import com.martinszuc.phishing_emails_detection.ui.component.import_emails.adapter.EmailAdapter
import com.martinszuc.phishing_emails_detection.ui.component.login.UserAccountViewModel
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
        }
    }

    private fun initSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // This method is called when the user submits the search string.
                query?.let {
                    emailViewModel.searchEmails(it)
                }

                hideKeyboard()

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

    private fun hideKeyboard() {
        val inputManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }
    // Show a loading spinner while a refresh operation is in progress

    private fun initLoadingSpinner() {
        viewLifecycleOwner.lifecycleScope.launch {
            emailAdapter.loadStateFlow.collectLatest { loadStates ->
                binding.loadingSpinner.visibility =
                    if (loadStates.refresh is LoadState.Loading) View.VISIBLE else View.GONE
                Log.d("EmailImportFragment", "Load state changed: ${loadStates.refresh}")
            }
        }
    }
}



