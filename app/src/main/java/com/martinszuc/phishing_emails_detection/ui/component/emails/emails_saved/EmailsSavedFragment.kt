package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.data_class.PhishyDialogResult
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailsSavedBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.EmailsDetailsDialogFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.adapter.EmailsSavedAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailFullSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailMinimalSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailParentSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class EmailsSavedFragment : Fragment(), EmailsDetailsDialogFragment.DialogDismissListener {
    private var _binding: FragmentEmailsSavedBinding? = null
    private val emailMinimalSharedViewModel: EmailMinimalSharedViewModel by activityViewModels()
    private val emailParentSharedViewModel: EmailParentSharedViewModel by activityViewModels()
    private val emailFullSharedViewModel: EmailFullSharedViewModel by activityViewModels()
    private val emailsSavedViewModel: EmailsSavedViewModel by activityViewModels()
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
        initObserveSelectedEmails()
        initFloatingActionButton() // TODO number of emails saved in the snackbar

        return binding.root
    }

    override fun onDialogDismissed() {
        emailMinimalSharedViewModel.clearIdFetchedEmail()
        emailFullSharedViewModel.clearIdFetchedEmail()
    }


    // Inside EmailsSavedFragment
    private fun initObserveSelectedEmails() {
        emailsSavedViewModel.selectedEmails.observe(viewLifecycleOwner) { selectedEmails ->
            // This may need a custom method in your adapter to properly refresh checkbox states without reloading all data
            emailsSavedAdapter.notifyDataSetChanged()
        }
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
                    emailParentSharedViewModel.setViewPagerPosition(0)
                }
            } else {
                // If the emails flow is not empty, hide the TextView and Button and show SearchView
                binding.emptySavedTextview.visibility = View.GONE
                binding.gotoSavedEmailsButton.visibility = View.GONE
            }
        }
    }

    private fun initFloatingActionButton() {
        val fab: FloatingActionButton = binding.fab

        // Set an observer on the selectedEmails LiveData
        emailsSavedViewModel.selectedEmails.observe(viewLifecycleOwner) { emails ->
            if (emails.isNotEmpty()) {
                fab.show()
            } else {
                fab.hide()
            }
        }

        fab.setOnClickListener {
            lifecycleScope.launch {
                val result = showPhishyConfirmationDialog()
                if (!result.wasCancelled && result.packageName != null) {
                    // Only proceed if the dialog was not cancelled and a package name was entered
                    emailsSavedViewModel.createEmailPackageFromSelected(result.isPhishy, result.packageName)
                    Toast.makeText(context, "Emails successfully packaged!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private suspend fun showPhishyConfirmationDialog(): PhishyDialogResult = suspendCoroutine { cont ->
        val context = requireContext()
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "Enter package name"
        input.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Show a Toast message
                Toast.makeText(context, "Please select if the package is phishing or safe.", Toast.LENGTH_LONG).show()
                true // Consume the event
            } else {
                false // Do not consume the event
            }
        }

        val dialog = AlertDialog.Builder(context).apply {
            setTitle("Confirm Email Package")
            setMessage("Is this email package suspicious (phishy)?")
            setView(input) // Add the input field to the dialog
            setPositiveButton("Yes") { _, _ ->
                cont.resume(PhishyDialogResult(isPhishy = true, packageName = input.text.toString()))
            }
            setNegativeButton("No") { _, _ ->
                cont.resume(PhishyDialogResult(isPhishy = false, packageName = input.text.toString()))
            }
            setNeutralButton("Cancel") { _, _ ->
                cont.resume(PhishyDialogResult(isPhishy = false, packageName = null, wasCancelled = true))
            }
            setCancelable(true)
            setOnCancelListener {
                cont.resume(PhishyDialogResult(isPhishy = false, packageName = null, wasCancelled = true))
            }

        }.create()

        dialog.show()
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Observe load state changes
        initLoadingSpinner() // TODO not used currently
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("EmailsSavedFragment", "onDestroyView called")
        emailsSavedViewModel.resetSelectionMode()
        _binding = null
    }

    private fun initEmailsSaved() {
        // Initialize RecyclerView and Adapter
        val recyclerView: RecyclerView = binding.emailSelectionRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        emailsSavedAdapter = EmailsSavedAdapter(emailsSavedViewModel) { emailId ->
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
            updateLoadingSpinner(isLoadingMinimal)
        }

        emailFullSharedViewModel.isLoading.observe(viewLifecycleOwner) { isLoadingFull ->
            updateLoadingSpinner(isLoadingFull)
        }
    }

    private fun updateLoadingSpinner(show: Boolean) {
        binding.loadingSpinner.visibility = if (show) View.GONE else View.VISIBLE
    }
}
