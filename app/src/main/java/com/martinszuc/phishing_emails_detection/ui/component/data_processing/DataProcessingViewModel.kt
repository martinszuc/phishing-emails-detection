package com.martinszuc.phishing_emails_detection.ui.component.data_processing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.model.DataProcessing
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DataProcessingViewModel @Inject constructor(
    private val dataProcessing: DataProcessing
    // Add other dependencies if needed
) : ViewModel() {

    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> = _isProcessing

    private val _isFinished = MutableLiveData<Boolean>()
    val isFinishedProcessing: LiveData<Boolean> = _isFinished

    init {
        _isProcessing.value = false
    }

    private suspend fun processEmailPackage(metadata: EmailPackageMetadata) {
        withContext(Dispatchers.Default) {
            // Perform CPU-intensive processing in the background thread
            dataProcessing.processMboxToCsv(
                Constants.DIR_EMAIL_PACKAGES,
                metadata.fileName,
                Constants.OUTPUT_CSV_DIR,
                "ascii", // Assuming you always want to use ASCII encoding
                500, // Assuming a default limit of 500
                metadata.isPhishy
            )
        }
    }

    fun processEmailPackages(packages: Set<EmailPackageMetadata>) {
        _isProcessing.value = true // Immediately indicate processing starts
        viewModelScope.launch {
            packages.forEach { metadata ->
                processEmailPackage(metadata)
            }
            _isProcessing.postValue(false) // Indicate processing ends after all packages are processed
            _isFinished.postValue(true) // Indicate that processing is finished completely
        }
    }

    fun clearIsFinished() {
        Log.d("DetectorViewModel", "Deselecting all emails")
        _isFinished.value = false
    }

}
