package com.martinszuc.phishing_emails_detection.data.email_package

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.martinszuc.phishing_emails_detection.data.AbstractManifestManager
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import java.io.File
import javax.inject.Inject

class EmailPackageManifestManager @Inject constructor(context: Context) :
    AbstractManifestManager<EmailPackageMetadata>(context) {

    override val manifestFileName = "emailPackageManifest.json"

    override fun getTypeToken() = object : TypeToken<List<EmailPackageMetadata>>() {}

    override fun refreshManifestFromDirectory(directory: File) {
        // No operation
    }

    fun removePackageFromManifest(fileName: String) {
        removeEntryFromManifest { it.fileName == fileName }
    }
}
