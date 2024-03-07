package com.martinszuc.phishing_emails_detection.data.email_package

import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import com.martinszuc.phishing_emails_detection.utils.emails.EmailUtils
import javax.inject.Inject

class EmailPackageManager @Inject constructor(
    private val emailBlobLocalRepository: EmailBlobLocalRepository,
    private val fileRepository: FileRepository,
    private val packageManifestManager: PackageManifestManager
) {
    suspend fun createEmailPackage(emailIds: List<String>, isPhishy: Boolean, packageName: String): String {
        val emailBlobs = emailBlobLocalRepository.getEmailBlobsByIds(emailIds)
        val mboxStrings = emailBlobs.map { EmailUtils.formatToMbox(it) }
        val mboxContent = EmailUtils.mergeMboxStrings(mboxStrings)
        val currentTimeFormatted = StringUtils.formatTimestampForFilename(System.currentTimeMillis())
        val filename = "${packageName}_${currentTimeFormatted}_${if (isPhishy) "phishy" else "safe"}.mbox"
        val filesize = fileRepository.getFileSizeInBytes(Constants.DIR_EMAIL_PACKAGES, filename)
        val numberOfEmails = emailBlobs.size


        // Save the package content to a file
        val file = fileRepository.saveMboxContent(mboxContent, "email_packages", filename)

        // Update the manifest with new package metadata
        val metadata = EmailPackageMetadata(filename, isPhishy, packageName, System.currentTimeMillis(), filesize, numberOfEmails)
        packageManifestManager.addPackageToManifest(metadata)

        // Return the file path
        return file.absolutePath
    }
}