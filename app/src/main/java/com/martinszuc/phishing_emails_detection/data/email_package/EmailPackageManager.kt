package com.martinszuc.phishing_emails_detection.data.email_package

import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.emails.EmailUtils
import java.io.File
import javax.inject.Inject

class EmailPackageManager @Inject constructor(
    private val emailBlobLocalRepository: EmailBlobLocalRepository,
    private val fileRepository: FileRepository,
    private val packageManifestManager: PackageManifestManager // Inject this dependency
) {
    suspend fun createEmailPackage(emailIds: List<String>, isPhishy: Boolean): String {
        val emailBlobs = emailBlobLocalRepository.getEmailBlobsByIds(emailIds)
        val mboxStrings = emailBlobs.map { EmailUtils.formatToMbox(it) }
        val mboxContent = EmailUtils.mergeMboxStrings(mboxStrings)
        val packageName = "emailPackage_${System.currentTimeMillis()}_${if (isPhishy) "phishy" else "safe"}.mbox"

        // Save the package content to a file
        val file = fileRepository.saveMboxContent(mboxContent, "emailPackages", packageName)

        // Update the manifest with new package metadata
        val metadata = EmailPackageMetadata(fileName = packageName, isPhishy = isPhishy, creationDate = System.currentTimeMillis())
        packageManifestManager.addPackageToManifest(metadata)

        // Return the file path
        return file.absolutePath
    }
}