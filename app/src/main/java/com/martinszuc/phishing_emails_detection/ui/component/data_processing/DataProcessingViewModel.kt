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
import javax.inject.Inject

@HiltViewModel
class DataProcessingViewModel @Inject constructor(
    private val dataProcessing: DataProcessing
) : AbstractBaseViewModel() {

    private suspend fun processEmailPackage(metadata: EmailPackageMetadata) {
        dataProcessing.processMboxToCsv(
            Constants.DIR_EMAIL_PACKAGES,
            metadata.fileName,
            Constants.OUTPUT_CSV_DIR,
            "ascii", // Placeholder for encoding
            500, // Placeholder for limit
            metadata.isPhishy
        )
    }

    fun processEmailPackages(packages: Set<EmailPackageMetadata>) {
        _hasStarted.value = true // Indicate the operation has started
        _isLoading.value = true // This reflects the process is ongoing
        _isFinished.value = false
        viewModelScope.launch {
            try {
                packages.forEach { metadata ->
                    viewModelScope.launch(Dispatchers.Default) {
                        processEmailPackage(metadata)
                    }
                }
                _isFinished.postValue(true) // Indicate that processing is complete
            } catch (e: Exception) {
                Log.e("DataProcessingViewModel", "Error processing email packages: ${e.message}")
                _operationFailed.postValue(true) // Indicate an error occurred
            } finally {
                _isLoading.postValue(false) // Reflect that processing has ended
                _hasStarted.postValue(false) // Reset hasStarted state
            }
        }
    }
}
