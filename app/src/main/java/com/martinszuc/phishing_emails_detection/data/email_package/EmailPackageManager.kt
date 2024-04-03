package com.martinszuc.phishing_emails_detection.data.email_package

import android.net.Uri
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailMboxLocalRepository
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import java.io.File
import javax.inject.Inject

class EmailPackageManager @Inject constructor(
    private val emailMboxLocalRepository: EmailMboxLocalRepository,
    private val fileRepository: FileRepository,
    private val packageManifestManager: PackageManifestManager
) {
    suspend fun createEmailPackage(
        emailIds: List<String>,
        isPhishy: Boolean,
        packageName: String
    ): String {
        val currentTime = System.currentTimeMillis()
        val currentTimeFormatted = StringUtils.formatTimestampForFilename(currentTime)
        val filename = "${packageName}_${currentTimeFormatted}_${if (isPhishy) "phishy" else "safe"}.mbox"

        // Initialize package file and metadata
        var numberOfEmails = 0

        emailIds.forEach { emailId ->
            // Fetch the formatted mbox string directly
            val emailMbox = emailMboxLocalRepository.fetchMboxContentById(emailId + ".mbox")
            emailMbox?.let {
                fileRepository.appendMboxContent(
                    Constants.DIR_EMAIL_PACKAGES,
                    filename,
                    it // Use the fetched mbox content
                )
                numberOfEmails++
            }
        }

        val fileSize = fileRepository.getFileSizeInBytes(Constants.DIR_EMAIL_PACKAGES, filename)

        val metadata = EmailPackageMetadata(
            fileName = filename,
            isPhishy = isPhishy,
            packageName = packageName,
            creationDate = currentTime,
            fileSize = fileSize,
            numberOfEmails = numberOfEmails
        )
        packageManifestManager.addPackageToManifest(metadata)

        return filename
    }

    suspend fun createAndSaveEmailPackageFromMbox(
        uri: Uri,
        isPhishy: Boolean,
        packageName: String
    ): Boolean? {
        val currentTime = System.currentTimeMillis()
        val currentTimeFormatted = StringUtils.formatTimestampForFilename(currentTime)
        // Initial temporary filename, without the email count
        val tempFilename =
            "${packageName}_${currentTimeFormatted}_${if (isPhishy) "phishy" else "safe"}.mbox"

        // Step 1: Copy the mbox file from the URI to internal storage with the temporary filename
        val copiedFile =
            fileRepository.copyFileFromUri(uri, Constants.DIR_EMAIL_PACKAGES, tempFilename)

        if (copiedFile != null) {
            // Step 2: Count the number of emails in the copied mbox file
            val numberOfEmails = fileRepository.countEmailsInMbox(copiedFile)

            // New filename that includes the email count
            val finalFilename =
                "${packageName}_${currentTimeFormatted}_${numberOfEmails}_${if (isPhishy) "phishy" else "safe"}.mbox"

            // Step 3: Rename the copied file to include the email count in its name
            val finalFile = File(copiedFile.parent, finalFilename)
            if (!copiedFile.renameTo(finalFile)) {
                // Handle the case where the file could not be renamed
                return null
            }

            // Step 4: Update the manifest with new package metadata
            val metadata = EmailPackageMetadata(
                fileName = finalFilename,
                isPhishy = isPhishy,
                packageName = packageName,
                creationDate = currentTime,
                fileSize = finalFile.length(),
                numberOfEmails = numberOfEmails // Use the generic itemCount field for email count
            )
            packageManifestManager.addPackageToManifest(metadata)

            return true
        } else {
            return null
        }
    }

}