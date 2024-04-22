package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detection

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailsDetectionBinding
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.EmailsDetailsDialogFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detection.adapter.EmailsDetectionAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailDetectionSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailFullSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailMinimalSharedViewModel
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Authored by matoszuc@gmail.com
 */

private const val logTag = "EmailsDetectionFragment"

@AndroidEntryPoint
class EmailsDetectionFragment : AbstractBaseFragment(), EmailsDetailsDialogFragment.DialogDismissListener {
    private var _binding: FragmentEmailsDetectionBinding? = null
    private val binding get() = _binding!!

    private val emailMinimalSharedViewModel: EmailMinimalSharedViewModel by activityViewModels()
    private val emailsDetectionSharedViewModel: EmailDetectionSharedViewModel by activityViewModels()
    private val emailFullSharedViewModel: EmailFullSharedViewModel by activityViewModels()
    private val emailDetectionSharedViewModel: EmailDetectionSharedViewModel by activityViewModels()
    private val emailsDetectionViewModel: EmailsDetectionViewModel by viewModels()

    private val emailDetailsCombined = MediatorLiveData<Pair<EmailMinimal?, EmailFull?>>()
    private var isDialogShown = false  // Flag to control dialog display

    private lateinit var emailsDetectionAdapter: EmailsDetectionAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEmailsDetectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEmailsDetection()
        observeEmailsFlow()
        initFloatingActionButton()
        setupEmailDetailsObserver()
    }

    private fun initEmailsDetection() {
        emailsDetectionAdapter = EmailsDetectionAdapter(emailsDetectionViewModel,
            { emailId ->
                // This lambda function is executed when an email item is clicked
                emailMinimalSharedViewModel.fetchEmailById(emailId) // Fetch minimal email details
                emailDetectionSharedViewModel.fetchEmailDetectionById(emailId)    // Fetch full email details
            },
            {
                // This lambda function is executed when the add email button is clicked
                openFilePicker() // Handle adding an email from file
            }
        )

        binding.emailSelectionRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = emailsDetectionAdapter
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = Constants.EML_FILE_TYPE
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerResultLauncher.launch(intent)
    }

    private val filePickerResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                // Call the suspend function within a coroutine scope
                lifecycleScope.launch {
                    val isPhishing = showIsPhishyDialog() // This will show the dialog and wait for the result
                    if (isPhishing) {
                        emailsDetectionViewModel.saveEmlToEmailDetection(uri, isPhishing) // Process the file if phishing is confirmed
                    }
                }
            }
        }
    }

    private suspend fun showIsPhishyDialog(): Boolean = suspendCoroutine { continuation ->
        val context = requireContext()
        val isPhishyCheckbox = CheckBox(context).apply {
            text = getString(R.string.phishing_label_2)

            // Adding margin top for spacing
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = resources.getDimensionPixelSize(R.dimen.spacing_16dp)
            layoutParams = params
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(isPhishyCheckbox)
        }

        val dialog = AlertDialog.Builder(context).apply {
            setTitle(getString(R.string.confirm_email))
            setView(layout)
            setPositiveButton(getString(R.string.confirm_big)) { _, _ ->
                continuation.resume(isPhishyCheckbox.isChecked) // Resume with the checkbox value
            }
            setNegativeButton(getString(R.string.cancel_big)) { _, _ ->
                continuation.resume(false) // Resume with false if cancelled
            }
            setCancelable(false) // Makes it mandatory to choose an option
        }.create()

        dialog.show()
    }

    override fun onDialogDismissed() {
        isDialogShown = false
        emailMinimalSharedViewModel.clearIdFetchedEmail()
        emailDetectionSharedViewModel.clearIdFetchedEmailDetection()
    }

    private fun setupEmailDetailsObserver() {
        var emailMinimal: EmailMinimal? = null
        var emailFull: EmailFull? = null

        emailDetailsCombined.addSource(emailMinimalSharedViewModel.emailById) { minimal ->
            emailMinimal = minimal
            if (emailMinimal != null && emailFull != null) {
                emailDetailsCombined.value = Pair(emailMinimal, emailFull)
            }
        }

        emailDetailsCombined.addSource(emailDetectionSharedViewModel.emailDetectionById) { emailDet ->
            emailFull = emailDet?.emailFull
            if (emailMinimal != null && emailFull != null) {
                emailDetailsCombined.value = Pair(emailMinimal, emailFull)
            }
        }

        emailDetailsCombined.observe(viewLifecycleOwner) { (minimal, full) ->
            if (minimal != null && full != null) {
                showEmailDetailsDialog(minimal, full)
            }
        }
    }

    private fun showEmailDetailsDialog(emailMinimal: EmailMinimal, emailFull: EmailFull) {
        if (!isDialogShown && isResumed) {  // Check if the fragment is currently resumed
            EmailsDetailsDialogFragment(emailMinimal, emailFull).apply {
                setDialogDismissListener(this@EmailsDetectionFragment)
                show(this@EmailsDetectionFragment.parentFragmentManager, logTag)
            }
            isDialogShown = true
        }
    }

    private fun observeEmailsFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            emailsDetectionSharedViewModel.localEmailDetectionsFlow.collectLatest { pagingData ->
                emailsDetectionAdapter.submitData(pagingData)
            }
        }
    }

    private fun initFloatingActionButton() {
        val fab: FloatingActionButton = binding.fab
        fab.setOnClickListener {
            // Handle FAB clicked
        }
    }

    override fun onDestroyView() {
        binding.emailSelectionRecyclerView.adapter = null
        lifecycleScope.cancel()
        super.onDestroyView()
        _binding = null
    }
}
