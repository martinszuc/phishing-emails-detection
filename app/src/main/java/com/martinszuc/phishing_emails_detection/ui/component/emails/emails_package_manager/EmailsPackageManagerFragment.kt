package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_package_manager

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
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
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
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_package_manager.adapter.EmailPackageAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailPackageSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailParentSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val logTag = "EmailsPackageManagerFragment"

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

    override fun onResume() {
        super.onResume()
        // Call loadEmailPackages when fragment is visible again
        emailPackageSharedViewModel.loadEmailPackages()
        Log.d(logTag, "Fragment resumed and packages reloaded")
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
        popup.menuInflater.inflate(R.menu.menu_epackage_add_options, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add_from_file -> {
                    // Handle "Add .mbox from File"
                    Log.d(logTag, "Add .mbox from File selected")
                    selectMboxFile()
                    true
                }

                R.id.action_build_package -> {
                    // Handle "Build Package Within App"
                    Log.d(logTag, "Build Package Within App selected")
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
                emailPackageSharedViewModel.loadEmailPackages()
                Toast.makeText(context, "Email package created!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun showPhishyConfirmationDialog(): PhishyDialogResult = suspendCoroutine { cont ->
        val context = requireContext()

        // Input field for the package name
        val packageNameInput = EditText(context).apply {
            hint = "Enter package name"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        // Checkbox for marking the package as phishy
        val isPhishyCheckbox = CheckBox(context).apply {
            text = context.getString(R.string.phishing_label_2)
        }

        // Layout to hold the inputs
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(packageNameInput)
            addView(isPhishyCheckbox)
        }

        val dialog = AlertDialog.Builder(context).apply {
            setTitle("Confirm Email Package")
            setView(layout) // Set the custom layout as the dialog view
            setPositiveButton(getString(R.string.confirm_big)) { _, _ ->
                // Resume coroutine with results from dialog
                cont.resume(PhishyDialogResult(
                    isPhishy = isPhishyCheckbox.isChecked,
                    packageName = packageNameInput.text.toString()
                ))
            }
            setNegativeButton(getString(R.string.cancel_big)) { _, _ ->
                // Handle cancellation
                cont.resume(PhishyDialogResult(isPhishy = false, packageName = null, wasCancelled = true))
            }
            setCancelable(true)
            setOnCancelListener {
                // Resume coroutine indicating cancellation
                cont.resume(PhishyDialogResult(isPhishy = false, packageName = null, wasCancelled = true))
            }
        }.create()

        dialog.show()
    }

    private fun observeViewModel() {
        emailPackageSharedViewModel.emailPackages.observe(viewLifecycleOwner) { packages ->
            Log.d(logTag, "Packages received: ${packages.size}")
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
        binding.rvEmailPackages.adapter = null
        lifecycleScope.cancel()
        super.onDestroyView()
        _binding = null
    }
}
