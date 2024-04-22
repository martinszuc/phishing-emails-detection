package com.martinszuc.phishing_emails_detection.ui.component.training

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.model.Training
import com.martinszuc.phishing_emails_detection.data.model_manager.ModelRepository
import com.martinszuc.phishing_emails_detection.data.processed_packages.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val training: Training,
    private val modelRepository: ModelRepository
) : AbstractBaseViewModel() { // Extend AbstractBaseViewModel

    private val _selectedPackages = MutableLiveData<Set<ProcessedPackageMetadata>>(setOf())
    val selectedPackages: LiveData<Set<ProcessedPackageMetadata>> = _selectedPackages

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


            viewModelScope.launch(Dispatchers.Default) {
                _isLoading.postValue(true) // Notify UI that the training process has started
                _isFinished.postValue(false)

                try {
                    training.trainModel(
                        Constants.OUTPUT_CSV_DIR,
                        safeFilename,
                        phishingFilename,
                        modelName
                    )

                    modelRepository.addModelToManifest(modelName)
                } finally {
                    _isLoading.postValue(false) // Notify UI that the training process has ended
                    _isFinished.postValue(true)
                }
            }
        } else {
            // Optionally handle case where there are not both phishing and safe packages selected
            _operationFailed.postValue(true)
        }
    }

}