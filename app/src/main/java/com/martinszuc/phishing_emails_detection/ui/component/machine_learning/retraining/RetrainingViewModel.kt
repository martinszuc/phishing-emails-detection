package com.martinszuc.phishing_emails_detection.ui.component.machine_learning.retraining

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.python.model.Retraining
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val logTag = "RetrainingViewModel"

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class RetrainingViewModel @Inject constructor(
    private val retraining: Retraining
) : AbstractBaseViewModel() { // Extend BaseViewModel

    private val _selectedPackages = MutableLiveData<Set<ProcessedPackageMetadata>>(setOf())
    val selectedPackages: LiveData<Set<ProcessedPackageMetadata>> = _selectedPackages

    private val _models = MutableLiveData<List<ModelMetadata>>()
    val models: LiveData<List<ModelMetadata>> = _models

    private val _selectedModel = MutableLiveData<ModelMetadata?>()
    val selectedModel: LiveData<ModelMetadata?> = _selectedModel

    fun togglePackageSelected(processedPackage: ProcessedPackageMetadata) {
        val currentSelectedPackages = _selectedPackages.value.orEmpty()
        _selectedPackages.value =
            if (currentSelectedPackages.any { it.fileName == processedPackage.fileName }) {
                currentSelectedPackages.filter { it.fileName != processedPackage.fileName }.toSet()
            } else {
                currentSelectedPackages + processedPackage
            }
    }

    fun toggleSelectedModel(modelMetadata: ModelMetadata) {
        _selectedModel.value = if (_selectedModel.value == modelMetadata) null else modelMetadata
    }

    fun startModelRetraining() {
        val phishingPackages = selectedPackages.value?.filter { it.isPhishy }?.map { it.fileName } ?: listOf()
        val safePackages = selectedPackages.value?.filter { !it.isPhishy }?.map { it.fileName } ?: listOf()
        val selectedModelName = selectedModel.value?.modelName

        if (phishingPackages.isNotEmpty() && safePackages.isNotEmpty() && selectedModelName != null) {
            launchDataLoad(execution = {
                retraining.retrainModel(
                    Constants.OUTPUT_CSV_DIR,
                    phishingPackages.joinToString(","),
                    safePackages.joinToString(","),
                    selectedModelName
                )
            }, onSuccess = {
                // Handle success if needed
            }, onFailure = { e ->
                Log.e(logTag, "Error during model retraining: ${e.message}")
            })
        } else {
            // Handle case where the required data is not available
            Log.e(logTag, "Invalid input for retraining")
            _operationFailed.postValue(true)
        }
    }

    fun clearSelectedPackages() {
        _selectedPackages.postValue(setOf())
    }

}
