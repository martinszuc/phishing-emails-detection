package com.martinszuc.phishing_emails_detection.ui.base

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.martinszuc.phishing_emails_detection.utils.Constants
import java.io.File

abstract class AbstractManifestManager<T>(protected val context: Context) {
    protected abstract val manifestFileName: String
    private val gson = Gson()

    private val manifestDirectory: File
        get() = File(context.filesDir, Constants.MANIFESTS_DIR).apply {
            if (!exists()) mkdir()
        }

    private val manifestFile: File
        get() = File(manifestDirectory, manifestFileName)

    protected abstract fun getTypeToken(): TypeToken<List<T>>

    fun loadManifest(): List<T> {
        if (!manifestFile.exists()) return emptyList()
        val json = manifestFile.readText()
        return gson.fromJson(json, getTypeToken().type)
    }

    fun saveManifest(manifest: List<T>) {
        manifestFile.writeText(gson.toJson(manifest))
    }

    fun addEntryToManifest(entry: T) {
        val currentManifest = loadManifest().toMutableList()
        currentManifest.add(entry)
        saveManifest(currentManifest)
    }

    fun removeEntryFromManifest(predicate: (T) -> Boolean) {
        val currentManifest = loadManifest().toMutableList()
        val isRemoved = currentManifest.removeAll(predicate)
        if (isRemoved) {
            saveManifest(currentManifest)
        }
    }

    abstract fun refreshManifestFromDirectory(directory: File)
}
