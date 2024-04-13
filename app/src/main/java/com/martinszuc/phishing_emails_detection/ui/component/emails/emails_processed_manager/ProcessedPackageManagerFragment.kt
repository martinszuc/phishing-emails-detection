package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_processed_manager

import ProcessedPackageAdapter
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
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailsProcessedPackageManagerBinding
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.ProcessedPackageSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailParentSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val logTag = "ProcessedPackageManagerFragment"

@AndroidEntryPoint
class ProcessedPackageManagerFragment : Fragment() {
    private var _binding: FragmentEmailsProcessedPackageManagerBinding? = null
    private val binding get() = _binding!!
    private val processedPackageManagerViewModel: ProcessedPackageManagerViewModel by viewModels()
    private val processedPackageSharedViewModel: ProcessedPackageSharedViewModel by activityViewModels()
    private val emailParentSharedViewModel: EmailParentSharedViewModel by activityViewModels()
    private lateinit var processedPackageAdapter: ProcessedPackageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailsProcessedPackageManagerBinding.inflate(inflater, container, false)
        setupRecyclerView()
        observeViewModel()
        initFloatingActionButton()
        return binding.root
    }

    private fun setupRecyclerView() {
        processedPackageAdapter = ProcessedPackageAdapter(
            emptyList(),
            onDeleteClicked = { fileName ->
                processedPackageManagerViewModel.deleteProcessedPackage(fileName)
                processedPackageSharedViewModel.refreshAndLoadProcessedPackages()
            },
            onAddClicked = { view ->
                showAddOptionsPopupMenu(view)
            }
        )
        binding.rvProcessedPackages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = processedPackageAdapter
        }
    }

    private fun showAddOptionsPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view, R.style.CustomPopupMenu)
        popup.menuInflater.inflate(R.menu.menu_processed_add_options, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add_from_file -> {
                    // Handle "Add .mbox from File"
                    Log.d(logTag, "Add .csv from File selected")
                    selectCsvFile()
                    true
                }

                R.id.action_build_package -> {
                    // Handle "Build Package Within App"
                    Log.d(logTag, "Build and Process Package Within App")
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
                onCsvFileSelected(uri)
            }
        }
    }

    private fun selectCsvFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerResultLauncher.launch(intent)
    }

    private fun onCsvFileSelected(uri: Uri) {
        lifecycleScope.launch {
            val result = showPhishyConfirmationDialog()
            if (!result.wasCancelled && result.packageName != null) {
                processedPackageManagerViewModel.createAndSaveProcessedPackageFromCsvFile(uri, result.isPhishy, result.packageName)
                processedPackageSharedViewModel.refreshAndLoadProcessedPackages()
                Toast.makeText(context, "Processed package created successfully.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun showPhishyConfirmationDialog(): PhishyDialogResult = suspendCoroutine { cont ->
        val context = requireContext()
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "Enter package name"
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Show a Toast message
                Toast.makeText(context, "Please select if the package is phishing or safe.", Toast.LENGTH_LONG).show()
                true // Consume the event
            } else {
                false // Do not consume the event
            }
        }

        val dialog = AlertDialog.Builder(context).apply {
            setTitle("Confirm Processed Package")
            setMessage("Is this processed package phishing?")
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

    private fun observeViewModel() {
        processedPackageSharedViewModel.processedPackages.observe(viewLifecycleOwner) { packages ->
            Log.d(logTag, "Packages received: ${packages.size}")
            processedPackageAdapter.setItems(packages)
        }
    }

    private fun initFloatingActionButton() {
        val fab: FloatingActionButton = binding.fab
        fab.show()
        fab.setOnClickListener {
            processedPackageSharedViewModel.refreshAndLoadProcessedPackages()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
