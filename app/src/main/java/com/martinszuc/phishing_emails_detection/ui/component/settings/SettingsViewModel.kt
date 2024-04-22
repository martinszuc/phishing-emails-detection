package com.martinszuc.phishing_emails_detection.ui.component.settings

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val logTag = "SettingsViewModel"

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : AbstractBaseViewModel() {

    fun clearMboxFiles() {
        launchDataLoad(
            execution = {
                fileRepository.clearDirectory(Constants.MBOX_FILES_DIR)
            },
            onSuccess = {
                // Optionally handle any success case, such as logging or updating a state variable
                Log.d(logTag, "Mbox files successfully cleared.")
            },
            onFailure = { e ->
                // Handle failure, e.g., log error or update a UI component
                Log.e(logTag, "Failed to clear Mbox files: ${e.message}")
            })
    }
}