package com.martinszuc.phishing_emails_detection.data.model_manager
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class ModelManifestManager(private val context: Context) {
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

    fun removeModelFromManifest(modelName: String) {
        val currentManifest = loadManifest().toMutableList()
        val modelToRemove = currentManifest.firstOrNull { it.modelName == modelName }
        modelToRemove?.let {
            currentManifest.remove(it)
            manifestFile.writeText(gson.toJson(currentManifest))
        }
    }
}
