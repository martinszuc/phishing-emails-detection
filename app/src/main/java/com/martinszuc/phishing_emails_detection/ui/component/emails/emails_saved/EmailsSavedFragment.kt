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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailsSavedBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.EmailsDetailsDialogFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.adapter.EmailsSavedAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailFullSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailMinimalSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class EmailsSavedFragment : Fragment(), EmailsDetailsDialogFragment.DialogDismissListener {
    private var _binding: FragmentEmailsSavedBinding? = null
    private val emailMinimalSharedViewModel: EmailMinimalSharedViewModel by activityViewModels()
    private val emailFullSharedViewModel: EmailFullSharedViewModel by activityViewModels()
    private val emailDetailsCombined = MediatorLiveData<Pair<EmailMinimal?, EmailFull?>>()
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
        initEmptyTextAndButton()
        observeEmailsFlow()
        setupEmailDetailsObserver()

        return binding.root
    }

    override fun onDialogDismissed() {
        emailMinimalSharedViewModel.clearIdFetchedEmail()
        emailFullSharedViewModel.clearIdFetchedEmail()
    }

    private fun initEmptyTextAndButton() {
        val viewPager: ViewPager2? = parentFragment?.view?.findViewById(R.id.view_pager)
        binding.searchView.visibility = View.VISIBLE

        emailsSavedAdapter.addLoadStateListener { loadState ->
            // Check if the current load state is empty.
            if (loadState.refresh is LoadState.NotLoading && emailsSavedAdapter.itemCount == 0) {
                // If the emails flow is empty, show the TextView and Button
                binding.emptySavedTextview.visibility = View.VISIBLE
                binding.gotoSavedEmailsButton.visibility = View.VISIBLE
                binding.gotoSavedEmailsButton.setOnClickListener {
                    // Navigate to the emails import fragment
                    viewPager?.currentItem = 1
                }
            } else {
                // If the emails flow is not empty, hide the TextView and Button and show SearchView
                binding.emptySavedTextview.visibility = View.GONE
                binding.gotoSavedEmailsButton.visibility = View.GONE
            }
        }
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
        // Initialize RecyclerView and Adapter
        val recyclerView: RecyclerView = binding.emailSelectionRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        emailsSavedAdapter = EmailsSavedAdapter { emailId ->
            emailMinimalSharedViewModel.fetchEmailById(emailId)
            emailFullSharedViewModel.fetchEmailById(emailId)
        }

        recyclerView.adapter = emailsSavedAdapter
    }

    private fun setupEmailDetailsObserver() {
        // Define temporary holders for your email data
        var emailMinimal: EmailMinimal? = null
        var emailFull: EmailFull? = null

        // Observe minimal email details
        emailDetailsCombined.addSource(emailMinimalSharedViewModel.emailById) { minimal ->
            emailMinimal = minimal
            if (emailMinimal != null && emailFull != null) {
                emailDetailsCombined.value = Pair(emailMinimal, emailFull)
            }
        }

        // Observe full email details
        emailDetailsCombined.addSource(emailFullSharedViewModel.emailById) { full ->
            emailFull = full
            if (emailMinimal != null && emailFull != null) {
                emailDetailsCombined.value = Pair(emailMinimal, emailFull)
            }
        }

        // Observe the combined LiveData for changes
        emailDetailsCombined.observe(viewLifecycleOwner) { (minimal, full) ->
            if (minimal != null && full != null) {
                showEmailDetailsDialog(minimal, full)
            }
        }
    }

    private fun showEmailDetailsDialog(emailMinimal: EmailMinimal, emailFull: EmailFull) {
        EmailsDetailsDialogFragment(emailMinimal, emailFull).apply {
            setDialogDismissListener(this@EmailsSavedFragment)
            show(this@EmailsSavedFragment.parentFragmentManager, "emailDetailsDialog")
        }
    }

    private fun observeEmailsFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                emailFullSharedViewModel.localEmailsFlow.collectLatest { pagingData ->
                    emailsSavedAdapter.submitData(pagingData)
                    binding.emailSelectionRecyclerView.layoutManager?.scrollToPosition(0)
                }
            }
        }
    }

    private fun initSearchView() {
        binding.searchView.visibility = View.GONE
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // This method is called when the user submits the search string.
                query?.let {
                    emailFullSharedViewModel.searchEmails(query)
                }

                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // This method is called when the query text is changed by the user.
                if (newText.isNullOrEmpty()) {
                    // If the query text is empty, call getEmails to fetch all emails.
                    emailFullSharedViewModel.getEmails()
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

    private fun initLoadingSpinner() {
        // Observing the loading state of both emails to control the spinner
        emailMinimalSharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoadingMinimal ->
            updateLoadingSpinner(isLoadingMinimal || emailFullSharedViewModel.isLoading.value == true)
        }

        emailFullSharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoadingFull ->
            updateLoadingSpinner(isLoadingFull || emailMinimalSharedViewModel.isLoading.value == true)
        }
    }

    private fun updateLoadingSpinner(show: Boolean) {
        binding.loadingSpinner.visibility = if (show) View.VISIBLE else View.GONE
    }
}
