package com.martinszuc.phishing_emails_detection.ui.component.data_processing

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.model.DataProcessing
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DataProcessingViewModel @Inject constructor(
    private val dataProcessing: DataProcessing
) : AbstractBaseViewModel() {
    private val logTag = "DataProcessingViewModel"

    fun processEmailPackages(packages: Set<EmailPackageMetadata>) {
        launchDataLoad(
            execution = {
                packages.forEach { metadata ->
                    processEmailPackage(metadata) // This function is assumed to be suspend
                }
            },
            onSuccess = {
                Log.e(logTag, "Success processing email packages")
            },
            onFailure = { e ->
                Log.e(logTag, "Error processing email packages: ${e.message}")
            }
        )
    }

    private suspend fun processEmailPackage(metadata: EmailPackageMetadata) {
        withContext(Dispatchers.Default) { // Ensure processing is done in the background
            dataProcessing.processMboxToCsv(
                Constants.DIR_EMAIL_PACKAGES,
                metadata.fileName,
                Constants.OUTPUT_CSV_DIR,
                "ascii", // Placeholder for encoding
                500, // Placeholder for limit
                metadata.isPhishy
            )
        }
    }
}
