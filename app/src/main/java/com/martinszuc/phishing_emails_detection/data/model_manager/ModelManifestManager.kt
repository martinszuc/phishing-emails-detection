package com.martinszuc.phishing_emails_detection.data.model_manager

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.martinszuc.phishing_emails_detection.data.model_manager.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.ui.base.AbstractManifestManager
import java.io.File
import java.util.Date
import javax.inject.Inject

class ModelManifestManager @Inject constructor(context: Context) :
    AbstractManifestManager<ModelMetadata>(context) {

    override val manifestFileName: String = "modelManifest.json"

    override fun getTypeToken(): TypeToken<List<ModelMetadata>> = object : TypeToken<List<ModelMetadata>>() {}

    override fun refreshManifestFromDirectory(directory: File) {
        val modelDirs = directory.listFiles { file -> file.isDirectory }

        val newManifest = modelDirs?.mapNotNull { dir ->
            // Assuming the directory name is used as the model's name
            // and the directory's last modified date as the creation date
            val modelName = dir.name
            val creationDate = Date(dir.lastModified())
            ModelMetadata(modelName, creationDate)
        } ?: emptyList()

        saveManifest(newManifest)
    }
}