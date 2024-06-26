package com.martinszuc.phishing_emails_detection.ui.component.model_manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.data_repository.auth.UserRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.file.FileRepository
import com.martinszuc.phishing_emails_detection.data.python.model.WeightManager
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.model_manager.ModelRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val logTag = "ModelManagerViewModel"

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class ModelManagerViewModel @Inject constructor(
    private val modelRepository: ModelRepository,
    private val weightManager: WeightManager,
    private val userRepository: UserRepository,
    private val fileRepository: FileRepository
) : AbstractBaseViewModel() {


    private val _selectedModel = MutableLiveData<ModelMetadata>()
    val selectedModel: LiveData<ModelMetadata> = _selectedModel

    fun toggleSelectedModel(modelMetadata: ModelMetadata) {
        _selectedModel.value = if (_selectedModel.value == modelMetadata) null else modelMetadata
    }

    fun uploadModelWeights() {
        _selectedModel.value?.let { model ->
            launchDataLoad(execution = {
                    _isLoading.postValue(true)
                    // Get user ID
                    val clientId = userRepository.getUserId()

                    // Extract weights filename
                    val weightsFilename = weightManager.extractModelWeights(model.modelName)

                    // Compress weights file and get path
                    val compressedFileName =
                        fileRepository.compressAndReturnName(Constants.WEIGHTS_DIR, weightsFilename)

                    // Upload the compressed weights
                    modelRepository.uploadCompressedWeights(clientId, compressedFileName)
            }, onSuccess = {
                Log.d(logTag, "Model weights uploaded successfully for model: ${model.modelName}")
            }, onFailure = { exception ->
                Log.e(logTag, "Error uploading model weights: ${exception.message}")
            })
        } ?: Log.e(logTag, "No model selected for uploading weights.")
    }


    fun downloadAndUpdateModelWeights() {
        _selectedModel.value?.let { model ->
            launchDataLoad(execution = {
                    modelRepository.downloadAndLoadModelWeights(model.modelName)
            }, onSuccess = {
                Log.d(logTag, "Model weights updated successfully for model: ${model.modelName}")
            }, onFailure = { exception ->
                Log.e(logTag, "Error downloading or updating model weights: ${exception.message}")
            })
        } ?: Log.e(logTag, "No model selected for downloading weights.")
    }

    fun deleteModelDirectory() {
        _selectedModel.value?.let { model ->
            launchDataLoad(execution = {
                modelRepository.deleteModelDirectory(model.modelName)
            }, onSuccess = {
                // Handle success if needed
                Log.d(logTag, "Model directory deleted successfully: ${model.modelName}")
            }, onFailure = { exception ->
                // Handle failure if needed
                Log.e(logTag, "Error deleting model directory: ${exception.message}")
            })
        }
    }
}