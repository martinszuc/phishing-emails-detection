package com.martinszuc.phishing_emails_detection.data.processed_packages

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.martinszuc.phishing_emails_detection.data.processed_packages.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import java.io.File
import javax.inject.Inject

class ProcessedPackageManifestManager @Inject constructor(private val context: Context) {
    private val gson = Gson()
    private val manifestFile = File(context.filesDir, "processedPackageManifest.json")

    fun refreshManifest(): List<ProcessedPackageMetadata> {
        if (!manifestFile.exists()) return emptyList()
        val json = manifestFile.readText()
        return gson.fromJson(json, object : TypeToken<List<ProcessedPackageMetadata>>() {}.type)
    }

    fun saveManifest(manifest: List<ProcessedPackageMetadata>) {
        manifestFile.writeText(gson.toJson(manifest))
    }

    fun addPackageToManifest(metadata: ProcessedPackageMetadata) {
        val currentManifest = this.refreshManifest().toMutableList()
        currentManifest.add(metadata)
        saveManifest(currentManifest)
    }

    fun removePackageFromManifest(fileName: String) {
        val currentManifest = this.refreshManifest().toMutableList()
        currentManifest.removeAll { it.fileName == fileName }
        saveManifest(currentManifest)
    }

    fun refreshManifestFromDirectory(directory: File) {
        val processedFiles = directory.listFiles { _, name -> name.endsWith("-export.csv") }
        val newManifest = processedFiles?.mapNotNull { file ->
            StringUtils.parseCsvFilename(file) // Passing the File object directly
        } ?: emptyList()

        saveManifest(newManifest)
    }
}
