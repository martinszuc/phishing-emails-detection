package com.martinszuc.phishing_emails_detection.data.data_repository.local.component.email_package

import android.net.Uri
import android.util.Log
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailMboxLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import java.io.File
import javax.inject.Inject

private const val logTag = "EmailPackageRepository"

/**
 * Provides methods to create, save, manage, and delete email packages. This repository handles interactions
 * with both the file system for mbox file operations and the local database for metadata management.
 * It supports creating packages from individual emails or directly from mbox files, managing their lifecycle,
 * and maintaining a manifest of all packages.
 *
 * @author matoszuc@gmail.com
 */
class EmailPackageRepository @Inject constructor(
    private val emailMboxLocalRepository: EmailMboxLocalRepository,
    private val fileRepository: FileRepository,
    private val emailPackageManifestManager: EmailPackageManifestManager
) {

    suspend fun createEmailPackage(
        emailIds: List<String>,
        isPhishy: Boolean,
        packageName: String,
        progressCallback: (Int) -> Unit  // Added a progress callback parameter
    ): String {
        Log.d(logTag, "Creating email package: $packageName")
        val currentTime = System.currentTimeMillis()
        val currentTimeFormatted = StringUtils.formatTimestampForFilename(currentTime)
        val tempFilename = "${packageName}_${currentTimeFormatted}_temp_${if (isPhishy) "phishy" else "safe"}.mbox"
        var numberOfEmails = 0

        emailIds.forEachIndexed { index, emailId ->
            val emailMbox = emailMboxLocalRepository.fetchMboxContentById("$emailId.mbox")
            emailMbox?.let {
                fileRepository.appendMboxContent(Constants.DIR_EMAIL_PACKAGES, tempFilename, it)
                numberOfEmails++
                fileRepository.deleteFile(Constants.MBOX_FILES_DIR, "$emailId.mbox")
                progressCallback(index + 1)  // Update progress after each email is processed
            }
        }

        val finalFilename = "${packageName}_${currentTimeFormatted}_${numberOfEmails}_${if (isPhishy) "phishy" else "safe"}.mbox"
        fileRepository.renameFile(Constants.DIR_EMAIL_PACKAGES, tempFilename, finalFilename)?.also {
            Log.d(logTag, "Package file renamed successfully to $finalFilename")
        } ?: Log.e(logTag, "Failed to rename package file to $finalFilename")

        fileRepository.getFileSizeInBytes(Constants.DIR_EMAIL_PACKAGES, finalFilename).also { fileSize ->
            val metadata = EmailPackageMetadata(finalFilename, isPhishy, packageName, currentTime, fileSize, numberOfEmails)
            emailPackageManifestManager.addEntryToManifest(metadata)
            Log.d(logTag, "Package metadata added for $finalFilename")
        }

        return finalFilename
    }


    suspend fun createAndSaveEmailPackageFromMbox(
        uri: Uri,
        isPhishy: Boolean,
        packageName: String
    ): Boolean {
        Log.d(logTag, "Creating email package from Mbox: $packageName")
        val currentTime = System.currentTimeMillis()
        val currentTimeFormatted = StringUtils.formatTimestampForFilename(currentTime)
        val tempFilename = "${packageName}_${currentTimeFormatted}_temp_${if (isPhishy) "phishy" else "safe"}.mbox"
        val copiedFile = fileRepository.copyFileFromUri(uri, Constants.DIR_EMAIL_PACKAGES, tempFilename) ?: run {
            Log.e(logTag, "Failed to copy file from URI for $packageName")
            return false
        }

        val numberOfEmails = fileRepository.countEmailsInMbox(copiedFile)
        val finalFilename = "${packageName}_${currentTimeFormatted}_${numberOfEmails}_${if (isPhishy) "phishy" else "safe"}.mbox"
        if (copiedFile.renameTo(File(copiedFile.parent, finalFilename))) {
            Log.d(logTag, "Package file renamed successfully to $finalFilename")
        } else {
            Log.e(logTag, "Failed to rename package file to $finalFilename")
            return false
        }

        val fileSize = fileRepository.getFileSizeInBytes(Constants.DIR_EMAIL_PACKAGES, finalFilename)
        val metadata = EmailPackageMetadata(finalFilename, isPhishy, packageName, currentTime, fileSize, numberOfEmails)
        emailPackageManifestManager.addEntryToManifest(metadata)
        Log.d(logTag, "Package metadata added for $finalFilename")

        return true
    }

    fun loadEmailPackagesMetadata(): List<EmailPackageMetadata> {
        Log.d(logTag, "Loading email packages metadata")
        return emailPackageManifestManager.loadManifest()
    }


    fun deleteEmailPackage(fileName: String) {
        Log.d(logTag, "Deleting email package: $fileName")
        emailPackageManifestManager.removePackageFromManifest(fileName)
        fileRepository.deleteFile(Constants.DIR_EMAIL_PACKAGES, fileName)
        Log.d(logTag, "$fileName deleted successfully")
    }
}
