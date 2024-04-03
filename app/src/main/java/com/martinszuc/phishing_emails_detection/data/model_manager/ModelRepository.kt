package com.martinszuc.phishing_emails_detection.data.model_manager

import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.data.model_manager.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.utils.Constants
import java.io.File
import java.util.Date

import javax.inject.Inject

class ModelRepository @Inject constructor(
    private val modelManifestManager: ModelManifestManager,
    private val fileRepository: FileRepository
) {
    fun loadModelsMetadata(): List<ModelMetadata> {
        return modelManifestManager.loadManifest()
    }

    fun addModelToManifest(modelName: String) {
        val creationDate = Date()
        val metadata = ModelMetadata(modelName, creationDate)
        modelManifestManager.addModelToManifest(metadata)
    }

    suspend fun refreshModelsFromDir() {
        val ModelsDirPath = fileRepository.getFilePath("", Constants.MODELS_DIR) ?: return
        val modelsDir = File(ModelsDirPath)

        modelManifestManager.refreshModelsFromDir(modelsDir)
    }
}