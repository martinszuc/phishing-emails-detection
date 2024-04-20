package com.martinszuc.phishing_emails_detection.data.model_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.data_class.Resource
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.data.model.WeightManager
import com.martinszuc.phishing_emails_detection.data.model_manager.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.data.model_manager.retrofit.ModelWeightsService
import com.martinszuc.phishing_emails_detection.utils.Constants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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

    suspend fun uploadCompressedWeights(clientId: String, fileName: String): Resource<String> {
        val file = fileRepository.loadFileFromDirectory(Constants.WEIGHTS_DIR, fileName)

        file?.let { safeFile ->
            val requestBody = safeFile.asRequestBody("application/gzip".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", safeFile.name, requestBody)
            val clientIdPart = MultipartBody.Part.createFormData("client_id", clientId)

            return try {
                val response = modelWeightsService.uploadWeights(clientIdPart, filePart)
                if (response.isSuccessful) {
                    Resource.Success("Upload successful")
                } else {
                    Resource.Error("Upload failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Resource.Error("Error during upload: ${e.message}")
            }
        } ?: return Resource.Error("Error: File not found or could not be loaded.")
    }



    suspend fun downloadAndLoadModelWeights(modelName: String) {
        try {
            val response = modelWeightsService.downloadWeights()

            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val weightsData = responseBody.byteStream().readBytes()
                    // Save the downloaded compressed weights to a temporary file
                    val tempWeightsFile = fileRepository.saveTemporaryWeights(weightsData, "temp_weights.gz")
                    val decompressedWeightsPath = fileRepository.decompressFile(tempWeightsFile)
                    val decompressedWeightsFile = File(decompressedWeightsPath)

                    weightManager.updateModelWithNewWeights(modelName, decompressedWeightsFile)
                    _downloadResult.postValue(Resource.Success(decompressedWeightsFile))
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