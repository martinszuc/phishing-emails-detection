package com.martinszuc.phishing_emails_detection.ui.component.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.EmailsSavedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.user.AccountSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    private val emailsSavedViewModel: EmailsSavedViewModel by activityViewModels()
    private val accountSharedViewModel: AccountSharedViewModel by activityViewModels()

    private lateinit var directoryPickerLauncher: ActivityResultLauncher<Intent> // Android 13 doesnt need explicit permissions to read external storage files. Android 10 and older do.

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Initialize the directory picker launcher
        directoryPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    copyModelToInternalStorage(uri)
                }
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "clear_database" -> {
                emailsSavedViewModel.clearDatabase()
                Toast.makeText(context, "Database cleared", Toast.LENGTH_SHORT).show()
            }
            "logout" -> {
                accountSharedViewModel.logout()
            }
            "load_model" -> {
                openDirectoryPicker()
            }
            "load_model_h5" -> {
                openFilePicker()
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun openDirectoryPicker() {
            // Launch the directory picker
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            directoryPickerLauncher.launch(intent)

    }

    private fun copyModelToInternalStorage(directoryUri: Uri) {
        try {
            val sourceDirectory = DocumentFile.fromTreeUri(requireContext(), directoryUri)
            sourceDirectory?.let { copyDocumentFileRecursively(it, requireContext().filesDir) }
            Toast.makeText(context, "Model copied successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to copy model", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyDocumentFileRecursively(file: DocumentFile, destinationDir: File) {
        if (file.isDirectory) {
            val newDir = File(destinationDir, file.name ?: "unknown")
            newDir.mkdirs()
            file.listFiles().forEach { childFile ->
                copyDocumentFileRecursively(childFile, newDir)
            }
        } else if (file.isFile) {
            val newFile = File(destinationDir, file.name ?: "unknown")
            requireContext().contentResolver.openInputStream(file.uri).use { input ->
                FileOutputStream(newFile).use { output ->
                    input?.copyTo(output)
                }
            }
        }
    }

    private fun openFilePicker() {
        // Launch the file picker
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*" // Specify the MIME type of H5 files here
        filePickerLauncher.launch(intent)
    }

    private val filePickerLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    // Check if the selected file is the desired H5 model file
                    val selectedFile = DocumentFile.fromSingleUri(requireContext(), uri)
//                    if (selectedFile != null && selectedFile.name == "classifier_tf_model.h5") {
                    if (selectedFile != null) {
                        copyH5ModelFile(selectedFile, requireContext().filesDir)
                        Toast.makeText(context, "Model copied successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Invalid file selected", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    private fun copyH5ModelFile(file: DocumentFile, destinationDir: File) {
        val newFile = File(destinationDir, "classifier_tf_model.h5")
        requireContext().contentResolver.openInputStream(file.uri).use { input ->
            FileOutputStream(newFile).use { output ->
                input?.copyTo(output)
            }
        }
    }
}
