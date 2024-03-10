package com.martinszuc.phishing_emails_detection.data.model_manager
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import java.io.File
import java.util.Date
import javax.inject.Inject

class ModelManifestManager @Inject constructor(private val context: Context) {
    private val gson = Gson()
    private val manifestFile = File(context.filesDir, "modelManifest.json")

    fun loadManifest(): List<ModelMetadata> {
        if (!manifestFile.exists()) return emptyList()
        val json = manifestFile.readText()
        return gson.fromJson(json, object : TypeToken<List<ModelMetadata>>() {}.type)
    }

    fun addModelToManifest(metadata: ModelMetadata) {
        val currentManifest = loadManifest().toMutableList()
        currentManifest.add(metadata)
        manifestFile.writeText(gson.toJson(currentManifest))
    }

    fun refreshModelsFromDir(modelsDir: File) {
        // Filter to get only directories, not files
        val modelDirs = modelsDir.listFiles { file -> file.isDirectory }

        val newManifest = modelDirs?.map { dir ->
            // The directory name is used as the model's name
            val modelName = dir.name
            // Use the directory's last modified date as the creation date
            val creationDate = Date(dir.lastModified())
            ModelMetadata(modelName, creationDate)
        } ?: emptyList()

        manifestFile.writeText(gson.toJson(newManifest))
    }
}


