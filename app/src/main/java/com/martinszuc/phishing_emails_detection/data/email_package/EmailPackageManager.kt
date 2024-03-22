package com.martinszuc.phishing_emails_detection.data.email_package

import android.net.Uri
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import com.martinszuc.phishing_emails_detection.utils.emails.EmailUtils
import java.io.File
import javax.inject.Inject

class EmailPackageManager @Inject constructor(
    private val emailBlobLocalRepository: EmailBlobLocalRepository,
    private val fileRepository: FileRepository,
    private val packageManifestManager: PackageManifestManager
) {
    suspend fun createEmailPackage(             // TODO need to write individual mbox into all at once not enough memory
        emailIds: List<String>,
        isPhishy: Boolean,
        packageName: String
    ): String {
        val emailBlobs = emailBlobLocalRepository.getEmailBlobsByIds(emailIds)
        val mboxStrings = emailBlobs.map { EmailUtils.formatToMbox(it) }
        val mboxContent = EmailUtils.mergeMboxStrings(mboxStrings)
        val currentTime = System.currentTimeMillis()
        val currentTimeFormatted = StringUtils.formatTimestampForFilename(currentTime)
        val numberOfEmails = emailBlobs.size
        val filename =
            "${packageName}_${currentTimeFormatted}_${numberOfEmails}_${if (isPhishy) "phishy" else "safe"}.mbox"


        // Save the package content to a file
        val filesize =
            fileRepository.saveTextToFileAndGetFileSize(mboxContent, "email_packages", filename)

        // Update the manifest with new package metadata
        val metadata = EmailPackageMetadata(
            filename,
            isPhishy,
            packageName,
            currentTime,
            filesize,
            numberOfEmails
        )
        packageManifestManager.addPackageToManifest(metadata)

        // Return the file path
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