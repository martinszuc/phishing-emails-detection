package com.martinszuc.phishing_emails_detection.data.data_repository.local.component.email_package

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.ui.base.AbstractManifestManager
import java.io.File
import javax.inject.Inject

/**
 * Manages the manifest file that tracks metadata about email packages. This class is responsible for
 * maintaining an updated list of all email packages, including their metadata like name, phishing status,
 * creation date, and size. It interfaces with the local file system to update and retrieve manifest data.
 *
 * @author matoszuc@gmail.com
 */
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
