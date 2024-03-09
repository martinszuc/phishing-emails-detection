package com.martinszuc.phishing_emails_detection.data.model_manager

import java.util.Date

import javax.inject.Inject

class ModelRepository @Inject constructor(
    private val modelManager: ModelManager,
    private val modelManifestManager: ModelManifestManager
) {
    fun loadModelsMetadata(): List<ModelMetadata> {
        return modelManifestManager.loadManifest()
    }

    suspend fun addModel(modelName: String, creationDate: Date) {
//        val creationDate = Date() // Capture the current date and time
        modelManager.createModel(modelName, creationDate)
    }
}