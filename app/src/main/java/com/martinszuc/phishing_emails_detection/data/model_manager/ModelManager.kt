package com.martinszuc.phishing_emails_detection.data.model_manager

import android.content.Context
import java.io.File
import java.util.Date
import javax.inject.Inject

class ModelManager @Inject constructor(
    private val modelManifestManager: ModelManifestManager
) {
    fun createModel(modelName: String, creationDate: Date) {
        // Assume the model is already created and saved in its directory
        // Add model metadata to manifest
        val metadata = ModelMetadata(modelName, creationDate)
        modelManifestManager.addModelToManifest(metadata)
    }

}