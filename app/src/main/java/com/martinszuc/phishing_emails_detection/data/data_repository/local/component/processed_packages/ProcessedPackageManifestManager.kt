package com.martinszuc.phishing_emails_detection.data.data_repository.local.component.processed_packages

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.ui.base.AbstractManifestManager
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import java.io.File
import javax.inject.Inject

class ProcessedPackageManifestManager @Inject constructor(context: Context) :
    AbstractManifestManager<ProcessedPackageMetadata>(context) {

    override val manifestFileName: String = "processedPackageManifest.json"

    override fun getTypeToken(): TypeToken<List<ProcessedPackageMetadata>> =
        object : TypeToken<List<ProcessedPackageMetadata>>() {}

    fun removePackageFromManifest(fileName: String) {
        removeEntryFromManifest { it.fileName == fileName }
    }

    override fun refreshManifestFromDirectory(directory: File) {
        val processedFiles = directory.listFiles { _, name -> name.endsWith("-export.csv") }
        val newManifest = processedFiles?.mapNotNull { file ->
            // Assuming StringUtils.parseCsvFilename can parse a file and return ProcessedPackageMetadata
            StringUtils.parseCsvFilename(file)
        } ?: emptyList()

        saveManifest(newManifest)
    }
}
