package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detector

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.data_class.PhishyDialogResult
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailPackageManagerBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detector.adapter.EmailPackageAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailPackageSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailParentSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Authored by matoszuc@gmail.com
 */
@AndroidEntryPoint
class EmailsPackageManagerFragment : Fragment() {
    private var _binding: FragmentEmailPackageManagerBinding? = null
    private val binding get() = _binding!!
    private lateinit var emailPackageAdapter: EmailPackageAdapter
    private val emailPackageManagerViewModel: EmailPackageManagerViewModel by viewModels()
    private val emailPackageSharedViewModel: EmailPackageSharedViewModel by activityViewModels()
    private val emailParentSharedViewModel: EmailParentSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailPackageManagerBinding.inflate(inflater, container, false)
        setupRecyclerView()
        observeViewModel()
        initFloatingActionButton()
        return binding.root
    }

    private fun setupRecyclerView() {
        emailPackageAdapter = EmailPackageAdapter(
            emptyList(),
            onDeleteClicked = { fileName ->
                emailPackageManagerViewModel.deleteEmailPackage(fileName)
                emailPackageSharedViewModel.loadEmailPackages()
            },
            onAddClicked = { view ->
                showAddOptionsPopupMenu(view)
            }
        )
        binding.rvEmailPackages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = emailPackageAdapter
        }
    }

    private fun showAddOptionsPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view, R.style.CustomPopupMenu)
        popup.menuInflater.inflate(R.menu.menu_add_options, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add_from_file -> {
                    // Handle "Add .mbox from File"
                    Log.d("PopupMenu", "Add .mbox from File selected")
                    selectMboxFile()
                    true
                }

                R.id.action_build_package -> {
                    // Handle "Build Package Within App"
                    Log.d("PopupMenu", "Build Package Within App selected")
                    emailParentSharedViewModel.setViewPagerPosition(0)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    private val filePickerResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                onMboxFileSelected(uri)
            }
        }
    }

    private fun selectMboxFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/mbox", "text/plain")) // MIME type for mbox might vary, adjust as needed
        }
        filePickerResultLauncher.launch(intent)
    }

    private fun onMboxFileSelected(uri: Uri) {
        lifecycleScope.launch {
            val result = showPhishyConfirmationDialog()
            if (!result.wasCancelled && result.packageName != null) {
                // Proceed with creating the email package from the selected mbox file
                // If copying is needed, use FileRepository for file operations
                emailPackageManagerViewModel.createAndSaveEmailPackageFromMboxFile(uri, result.isPhishy, result.packageName)
                Toast.makeText(context, "Email package created successfully.", Toast.LENGTH_SHORT).show()
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

    companion object {
        private const val REQUEST_CODE_SELECT_MBOX = 1001
    }

    private fun observeViewModel() {
        emailPackageSharedViewModel.emailPackages.observe(viewLifecycleOwner) { packages ->
            Log.d("EmailsPackageManager", "Packages received: ${packages.size}")
            emailPackageAdapter.setItems(packages)
        }
    }

    private fun initFloatingActionButton() {
        val fab: FloatingActionButton = binding.fab
        fab.show()
        fab.setOnClickListener {
            emailPackageSharedViewModel.loadEmailPackages()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
