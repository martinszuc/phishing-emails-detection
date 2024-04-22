package com.martinszuc.phishing_emails_detection.data.model_manager

import android.util.Log
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

private const val logTag = "ModelRepository"

/**
 * Authored by matoszuc@gmail.com
 */

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
        Log.d(logTag, "Attempting to upload weights: $fileName for client ID: $clientId")

        val file = fileRepository.loadFileFromDirectory(Constants.WEIGHTS_DIR, fileName)

        file?.let { safeFile ->
            Log.d(logTag, "File loaded successfully: ${safeFile.path}")

            val requestBody = safeFile.asRequestBody("application/gzip".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", safeFile.name, requestBody)
            val clientIdPart = MultipartBody.Part.createFormData("client_id", clientId)

            return try {
                Log.d(logTag, "Initiating upload for client ID: $clientId")
                val response = modelWeightsService.uploadWeights(clientIdPart, filePart)
                if (response.isSuccessful) {
                    Log.d(logTag, "Weights upload successful for client ID: $clientId")
                    Resource.Success("Upload successful")
                } else {
                    val errorResponse = response.errorBody()?.string() ?: "Unknown error"
                    Log.e(logTag, "Upload failed: $errorResponse")
                    Resource.Error("Upload failed: $errorResponse")
                }
            } catch (e: Exception) {
                Log.e(logTag, "Error during upload: ${e.message}")
                Resource.Error("Error during upload: ${e.message}")
            }
        } ?: run {
            Log.e(logTag, "Error: File not found or could not be loaded: $fileName")
            return Resource.Error("Error: File not found or could not be loaded.")
        }
    }



    suspend fun downloadAndLoadModelWeights(modelName: String) {
        try {
            val response = modelWeightsService.downloadWeights()
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val weightsData = responseBody.byteStream().readBytes()
                    val tempWeightsFile = fileRepository.saveTemporaryWeights(weightsData, "temp_weights.gz")
                    Log.d(logTag, "Temporary weights file saved: ${tempWeightsFile.path}")

                    val decompressedWeightsPath = fileRepository.decompressFile(tempWeightsFile)
                    Log.d(logTag, "Weights decompressed to: $decompressedWeightsPath")

                    val decompressedWeightsFile = File(decompressedWeightsPath)
                    weightManager.updateModelWithNewWeights(modelName, decompressedWeightsFile)
                    _downloadResult.postValue(Resource.Success(decompressedWeightsFile))
                    Log.d(logTag, "Weights update initiated for model: $modelName")
                } ?: run {
                    _downloadResult.postValue(Resource.Error("Download failed: Empty response", null))
                    Log.e(logTag, "Download failed: Empty response")
                }
            } else {
                _downloadResult.postValue(Resource.Error("Download failed: ${response.errorBody()?.string()}", null))
                Log.e(logTag, "Download failed with error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            _downloadResult.postValue(Resource.Error("Network error: ${e.message}", null))
            Log.e(logTag, "Error during weights download and model update with message: ${e.message}")
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