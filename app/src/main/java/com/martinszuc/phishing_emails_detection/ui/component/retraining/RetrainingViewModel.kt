package com.martinszuc.phishing_emails_detection.ui.component.retraining

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.model.Retraining
import com.martinszuc.phishing_emails_detection.data.model_manager.ModelMetadata
import com.martinszuc.phishing_emails_detection.data.model_manager.ModelRepository
import com.martinszuc.phishing_emails_detection.data.processed_packages.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RetrainingViewModel @Inject constructor(
    private val retraining: Retraining
        // Add other dependencies if needed
) : ViewModel() {

    private val _selectedPackages = MutableLiveData<Set<ProcessedPackageMetadata>>(setOf())
    val selectedPackages: LiveData<Set<ProcessedPackageMetadata>> = _selectedPackages

    private val _models = MutableLiveData<List<ModelMetadata>>()
    val models: LiveData<List<ModelMetadata>> = _models

    private val _selectedModel = MutableLiveData<ModelMetadata?>()
    val selectedModel: LiveData<ModelMetadata?> = _selectedModel

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

    fun toggleSelectedModel(modelMetadata: ModelMetadata) {
        val currentSelectedModel = _selectedModel.value
        if (currentSelectedModel == modelMetadata) {
            // If the same model is selected again, deselect it
//            _selectedModel.value = null
        } else {
            // Select the new model
            _selectedModel.value = modelMetadata
        }
    }
    fun startModelRetraining() {
        val phishingPackages = selectedPackages.value?.filter { it.isPhishy }?.map { it.fileName } ?: listOf()
        val safePackages = selectedPackages.value?.filter { !it.isPhishy }?.map { it.fileName } ?: listOf()
        val selectedModelName = selectedModel.value?.modelName

        if (phishingPackages.isNotEmpty() && safePackages.isNotEmpty() && selectedModelName != null) {
            val phishingFilename = phishingPackages.first() // Consider how you want to handle multiple files
            val safeFilename = safePackages.first() // Consider how you want to handle multiple files

            viewModelScope.launch(Dispatchers.IO) {
                _isLoading.postValue(true) // Notify UI that the retraining process has started
                _isFinished.postValue(false)

                try {
                    retraining.retrainModel(
                        Constants.OUTPUT_CSV_DIR,
                        safeFilename,
                        phishingFilename,
                        selectedModelName
                    )

                    // Here, instead of adding a new model, you might update the model info in the repository
                    // modelRepository.updateModel(selectedModelName)
                } finally {
                    _isLoading.postValue(false) // Notify UI that the retraining process has ended
                    _isFinished.postValue(true)
                }
            }
        } else {
            // Optionally handle case where there are not both phishing and safe packages selected
            // or when no model is selected
        }
    }


}
