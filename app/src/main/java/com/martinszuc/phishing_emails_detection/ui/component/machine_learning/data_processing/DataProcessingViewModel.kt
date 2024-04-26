package com.martinszuc.phishing_emails_detection.ui.component.machine_learning.data_processing

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.python.model.DataProcessing
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val logTag = "DataProcessingViewModel"

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class DataProcessingViewModel @Inject constructor(
    private val dataProcessing: DataProcessing
) : AbstractBaseViewModel() {

    fun processEmailPackages(packages: Set<EmailPackageMetadata>) {
        launchDataLoad(
            execution = {
                packages.forEach { metadata ->
                    processEmailPackage(metadata) // This function is assumed to be suspend
                }
            },
            onSuccess = {
                Log.i(logTag, "Success processing email packages")
            },
            onFailure = { e ->
                Log.e(logTag, "Error processing email packages: ${e.message}")
            }
        )
    }

    private fun processEmailPackage(metadata: EmailPackageMetadata) {
        dataProcessing.processMboxToCsv(
            Constants.DIR_EMAIL_PACKAGES,
            metadata.fileName,
            Constants.OUTPUT_CSV_DIR,
            "utf-8", // Placeholder for encoding
            2000, // Placeholder for limit
            metadata.isPhishy
        )

    }
}
