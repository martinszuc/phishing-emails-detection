package com.martinszuc.phishing_emails_detection.data.model_manager

import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import java.io.File
import java.util.Date
import javax.inject.Inject

class ModelManager @Inject constructor(
    private val modelManifestManager: ModelManifestManager,
    private val fileRepository: FileRepository
) {
    fun createModel(modelName: String, creationDate: Date) {
        // Assume the model is already created and saved in its directory
        // Add model metadata to manifest
        val metadata = ModelMetadata(modelName, creationDate)
        modelManifestManager.addModelToManifest(metadata)
    }
    suspend fun refreshModelsFromDir() {
        val ModelsDirPath = fileRepository.getFilePath("", Constants.MODELS_DIR) ?: return
        val modelsDir = File(ModelsDirPath)

        modelManifestManager.refreshModelsFromDir(modelsDir)
    }



}