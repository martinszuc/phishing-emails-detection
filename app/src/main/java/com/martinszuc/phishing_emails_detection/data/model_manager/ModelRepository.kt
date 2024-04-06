package com.martinszuc.phishing_emails_detection.data.model_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.data_class.Resource
import com.martinszuc.phishing_emails_detection.data.data_class.WeightData
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.data.model.WeightManager
import com.martinszuc.phishing_emails_detection.data.model_manager.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.data.model_manager.retrofit.ModelWeightsService
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import java.io.File
import java.util.Date
import javax.inject.Inject

class ModelRepository @Inject constructor(
    private val modelManifestManager: ModelManifestManager,
    private val fileRepository: FileRepository,
    private val modelWeightsService: ModelWeightsService,
    private val weightManager: WeightManager
) {
    private val _uploadResult = MutableLiveData<Resource<String>>()
    val uploadResult: LiveData<Resource<String>> = _uploadResult

    private val _downloadResult = MutableLiveData<Resource<File>>()
    val downloadResult: LiveData<Resource<File>> = _downloadResult

    suspend fun uploadModelWeights(modelName: String, weightsJson: String) {
        try {
            val clientId = StringUtils.generateClientId() // Implement this to generate or retrieve a unique client ID
            val response = modelWeightsService.uploadWeights(WeightData(clientId, weightsJson))

            if (response.isSuccessful) {
                _uploadResult.postValue(Resource.Success("Upload successful"))
            } else {
                _uploadResult.postValue(Resource.Error("Upload failed: ${response.errorBody()?.string()}", null))
            }
        } catch (e: Exception) {
            _uploadResult.postValue(Resource.Error("Network error: ${e.message}", null))
        }
    }

    suspend fun downloadModelWeights(modelName: String) {
        try {
            val response = modelWeightsService.downloadWeights()

            if (response.isSuccessful) {
                response.body()?.let { weightsResponse ->
                    // Save the downloaded weights to a temporary file
                    val tempWeightsFile = fileRepository.saveTemporaryWeights(weightsResponse.weights)
                    // Now tempWeightsFile contains the latest weights
                    _downloadResult.postValue(Resource.Success(tempWeightsFile))
                    weightManager.updateModelWithNewWeights(modelName, tempWeightsFile.name)
                } ?: run {
                    _downloadResult.postValue(Resource.Error("Download failed: Empty response", null))
                }
            } else {
                _downloadResult.postValue(Resource.Error("Download failed: ${response.errorBody()?.string()}", null))
            }
        } catch (e: Exception) {
            _downloadResult.postValue(Resource.Error("Network error: ${e.message}", null))
        }
    }


    fun loadModelsMetadata(): List<ModelMetadata> {
        return modelManifestManager.loadManifest()
    }

    fun addModelToManifest(modelName: String) {
        val creationDate = Date()
        val metadata = ModelMetadata(modelName, creationDate)
        modelManifestManager.addEntryToManifest(metadata)
    }

    suspend fun refreshModelsFromDir() {
        val ModelsDirPath = fileRepository.getFilePath("", Constants.MODELS_DIR) ?: return
        val modelsDir = File(ModelsDirPath)

        modelManifestManager.refreshManifestFromDirectory(modelsDir)
    }

}