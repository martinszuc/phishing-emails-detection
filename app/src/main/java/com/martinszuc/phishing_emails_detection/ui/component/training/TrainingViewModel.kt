package com.martinszuc.phishing_emails_detection.ui.component.training

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.email_package.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.data.model.DataProcessing
import com.martinszuc.phishing_emails_detection.data.model.Training
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val training: Training
    // Add other dependencies if needed
) : ViewModel() {

    private val _selectedPackages = MutableLiveData<Set<ProcessedPackageMetadata>>(setOf())
    val selectedPackages: LiveData<Set<ProcessedPackageMetadata>> = _selectedPackages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFinished = MutableLiveData<Boolean>()
    val isFinished: LiveData<Boolean> = _isFinished

    fun togglePackageSelected(processedPackage: ProcessedPackageMetadata) {
        val currentSelectedPackages = _selectedPackages.value.orEmpty()
        _selectedPackages.value =
            if (currentSelectedPackages.any { it.fileName == processedPackage.fileName }) {
                currentSelectedPackages.filter { it.fileName != processedPackage.fileName }.toSet()
            } else {
                currentSelectedPackages + processedPackage
            }
    }

    fun startModelTraining(modelName: String) {
        val phishingPackages = selectedPackages.value?.filter { it.isPhishy }?.map { it.fileName } ?: listOf()
        val safePackages = selectedPackages.value?.filter { !it.isPhishy }?.map { it.fileName } ?: listOf()

        if (phishingPackages.isNotEmpty() && safePackages.isNotEmpty()) {
            val phishingFilename = phishingPackages.first()
            val safeFilename = safePackages.first()

            viewModelScope.launch(Dispatchers.IO) {
                _isLoading.postValue(true) // Notify UI that the training process has started
                _isFinished.postValue(false)

                try {
                    training.trainModel(
                        Constants.OUTPUT_CSV_DIR,
                        safeFilename,
                        phishingFilename,
                        modelName
                    )
                } finally {
                    _isLoading.postValue(false) // Notify UI that the training process has ended
                    _isFinished.postValue(true)
                }
            }
        } else {
            // Optionally handle case where there are not both phishing and safe packages selected
        }
    }

}