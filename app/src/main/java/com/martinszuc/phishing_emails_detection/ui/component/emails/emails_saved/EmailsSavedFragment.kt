package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailsSavedBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.adapter.EmailsSavedAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Authored by matoszuc@gmail.com
 */
// TODO When empty refer to import fragment
@AndroidEntryPoint
class EmailsSavedFragment : Fragment() {
    private var _binding: FragmentEmailsSavedBinding? = null
    private val emailsSavedViewModel: EmailsSavedViewModel by viewModels()
    private lateinit var emailsSavedAdapter: EmailsSavedAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("EmailsSavedFragment", "onCreateView called")
        _binding = FragmentEmailsSavedBinding.inflate(inflater, container, false)

        initEmailsSaved()
        initSearchView()
        observeEmailsFlow()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Observe load state changes
        initLoadingSpinner()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("EmailsSavedFragment", "onDestroyView called")
        _binding = null
    }

    private fun initEmailsSaved() {
        // Initialize RecyclerView
        val recyclerView: RecyclerView = binding.emailSelectionRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize EmailAdapter
        emailsSavedAdapter = EmailsSavedAdapter(emailsSavedViewModel)
        recyclerView.adapter = emailsSavedAdapter
    }

    private fun observeEmailsFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                emailsSavedViewModel.emailsFlow.collectLatest { pagingData ->
                    emailsSavedAdapter.submitData(pagingData)
                    binding.emailSelectionRecyclerView.layoutManager?.scrollToPosition(0)  // Add this line
                }
            }
        }
    }

    private fun initSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // This method is called when the user submits the search string.
                query?.let {
                    emailsSavedViewModel.searchEmails(query)
                }

                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // This method is called when the query text is changed by the user.
                if (newText.isNullOrEmpty()) {
                    // If the query text is empty, call getEmails to fetch all emails.
                    emailsSavedViewModel.getEmails()
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
            emailsSavedAdapter.loadStateFlow.collectLatest { loadStates ->
                binding.loadingSpinner.visibility =
                    if (loadStates.refresh is LoadState.Loading) View.VISIBLE else View.GONE
                Log.d("EmailImportFragment", "Load state changed: ${loadStates.refresh}")
            }
        }
    }
}
