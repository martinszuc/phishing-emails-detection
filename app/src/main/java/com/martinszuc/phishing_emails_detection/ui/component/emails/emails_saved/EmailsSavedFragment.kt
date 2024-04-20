package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Authored by matoszuc@gmail.com
 */

private const val logTag = "EmailsSavedFragment"

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
    private var isDialogShown = false  // Flag to track if the dialog is shown


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(logTag, "onCreateView called")
        _binding = FragmentEmailsSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isDialogShown = false
        initEmailsSaved()
        initSearchView()
        initEmptyTextAndButton()
        observeEmailsFlow()
        setupEmailDetailsObserver()
        initObserveSelectedEmails()
        initFloatingActionButton()
        initBatchPackageButton()
        initLoadingFrame()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(logTag, "onDestroyView called")
        emailsSavedViewModel.resetSelectionMode()
        _binding = null
    }

    private fun initBatchPackageButton() {
        binding.btnBatchSave.setOnClickListener {
            showCreatePackageDialog()
        }
    }

    override fun onDialogDismissed() {
        isDialogShown = false  // Reset the flag when dialog is dismissed
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
                    emailsSavedViewModel.createEmailPackageFromSelected(
                        result.isPhishy,
                        result.packageName
                    )
                    Toast.makeText(context, "Emails successfully packaged!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }


    private suspend fun showPhishyConfirmationDialog(): PhishyDialogResult =
        suspendCoroutine { cont ->
            val context = requireContext()
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT
            input.hint = "Enter package name"
            input.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Show a Toast message
                    Toast.makeText(
                        context,
                        "Please select if the package is phishing or safe.",
                        Toast.LENGTH_LONG
                    ).show()
                    true // Consume the event
                } else {
                    false // Do not consume the event
                }
            }

            val dialog = AlertDialog.Builder(context).apply {
                setTitle("Confirm Email Package")
                setMessage("Is this package phishing?")
                setView(input) // Add the input field to the dialog
                setPositiveButton(getString(R.string.yes)) { _, _ ->
                    cont.resume(
                        PhishyDialogResult(
                            isPhishy = true,
                            packageName = input.text.toString()
                        )
                    )
                }
                setNegativeButton(getString(R.string.no)) { _, _ ->
                    cont.resume(
                        PhishyDialogResult(
                            isPhishy = false,
                            packageName = input.text.toString()
                        )
                    )
                }
                setNeutralButton(getString(R.string.cancel_big)) { _, _ ->
                    cont.resume(
                        PhishyDialogResult(
                            isPhishy = false,
                            packageName = null,
                            wasCancelled = true
                        )
                    )
                }
                setCancelable(true)
                setOnCancelListener {
                    cont.resume(
                        PhishyDialogResult(
                            isPhishy = false,
                            packageName = null,
                            wasCancelled = true
                        )
                    )
                }

            }.create()

            dialog.show()
        }

    private fun showCreatePackageDialog() {
        val context = requireContext()
        val packageNameInput = EditText(context).apply {
            hint = "Enter package name"
        }
        val emailCountInput = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Enter number of emails to include"
        }
        val isPhishyCheckbox = CheckBox(context).apply {
            text = context.getString(R.string.phishing_label_2)
        }
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(packageNameInput)
            addView(emailCountInput)
            addView(isPhishyCheckbox)
        }

        AlertDialog.Builder(context).apply {
            setTitle("Create Email Package")
            setView(layout)
            setPositiveButton("Create") { _, _ ->
                val packageName = packageNameInput.text.toString()
                val limit = emailCountInput.text.toString().toIntOrNull() ?: 0
                val isPhishy = isPhishyCheckbox.isChecked
                if (packageName.isNotBlank() && limit > 0) {
                    emailsSavedViewModel.createEmailPackageFromLatest(isPhishy, packageName, limit)
                } else {
                    Toast.makeText(
                        context,
                        "Invalid package name or email count",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            setNegativeButton(getString(R.string.cancel_big), null)
            show()
        }
    }

    private fun initEmailsSaved() {
        // Initialize the EmailsSavedAdapter with necessary callbacks
        emailsSavedAdapter = EmailsSavedAdapter(
            emailsSavedViewModel,
            { emailId ->
                // This lambda function is executed when an email item is clicked
                emailMinimalSharedViewModel.fetchEmailById(emailId) // Fetch minimal email details
                emailFullSharedViewModel.fetchEmailById(emailId)    // Fetch full email details
            },
            {
                // This lambda function is executed when the add email button is clicked
                addEmailFromFile() // Handle adding an email from file
            }
        )

        // Set up the RecyclerView with a LinearLayoutManager and attach the adapter
        binding.emailSelectionRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = emailsSavedAdapter
        }
    }

    private fun addEmailFromFile() {
        // Open a file picker to select an EML file
        openFilePicker()
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
        if (!isDialogShown && isResumed) {  // Check if the fragment is currently resumed
            EmailsDetailsDialogFragment(emailMinimal, emailFull).apply {
                setDialogDismissListener(this@EmailsSavedFragment)
                show(this@EmailsSavedFragment.parentFragmentManager, logTag)
            }
            isDialogShown = true  // Set the flag to true as the dialog is now being shown
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

    private fun initLoadingFrame() {
        emailsSavedViewModel.totalCount.observe(viewLifecycleOwner) { total ->
            if (total > 0) {
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.progressBar.max = total
                binding.progressText.text = "0 / $total"
            } else {
                binding.loadingOverlay.visibility = View.GONE
            }
        }

        emailsSavedViewModel.progress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress
            binding.progressText.text = "$progress / ${binding.progressBar.max}"
            if (progress == binding.progressBar.max) {
                binding.loadingOverlay.visibility = View.GONE
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = Constants.EML_FILE_TYPE  // Adjust as necessary
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerResultLauncher.launch(intent)
    }

    private val filePickerResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                emailsSavedViewModel.createEmailFullFromEML(uri)
                Toast.makeText(context, "File processed successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
