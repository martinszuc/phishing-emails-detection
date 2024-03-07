package com.martinszuc.phishing_emails_detection.data.email_package

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import java.io.File
import javax.inject.Inject

class PackageManifestManager @Inject constructor(private val context: Context) {

    private val gson = Gson()
    private val manifestFile = File(context.filesDir, "emailPackageManifest.json")

    fun loadManifest(): List<EmailPackageMetadata> {
        if (!manifestFile.exists()) return emptyList()
        val json = manifestFile.readText()
        return gson.fromJson(json, object : TypeToken<List<EmailPackageMetadata>>() {}.type)
    }

    fun addPackageToManifest(metadata: EmailPackageMetadata) {
        val currentManifest = loadManifest().toMutableList()
        currentManifest.add(metadata)
        manifestFile.writeText(gson.toJson(currentManifest))
    }

    fun removePackageFromManifest(fileName: String) {
        val currentManifest = loadManifest().toMutableList()
        val packageToRemove = currentManifest.firstOrNull { it.fileName == fileName }
        if (packageToRemove != null) {
            currentManifest.remove(packageToRemove)
            manifestFile.writeText(gson.toJson(currentManifest))
        }
    }
}