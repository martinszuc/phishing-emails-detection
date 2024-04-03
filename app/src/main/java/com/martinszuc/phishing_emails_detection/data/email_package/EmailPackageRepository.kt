package com.martinszuc.phishing_emails_detection.data.email_package

import android.net.Uri
import android.util.Log
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailMboxLocalRepository
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import java.io.File
import javax.inject.Inject

class EmailPackageRepository @Inject constructor(
    private val emailMboxLocalRepository: EmailMboxLocalRepository,
    private val fileRepository: FileRepository,
    private val emailPackageManifestManager: EmailPackageManifestManager
) {
    private val logTag = "EmailPackageRepository"

    suspend fun createEmailPackage(
        emailIds: List<String>,
        isPhishy: Boolean,
        packageName: String
    ): String {
        Log.d(logTag, "Creating email package: $packageName")
        val currentTime = System.currentTimeMillis()
        val currentTimeFormatted = StringUtils.formatTimestampForFilename(currentTime)
        val tempFilename = "${packageName}_${currentTimeFormatted}_temp_${if (isPhishy) "phishy" else "safe"}.mbox"
        var numberOfEmails = 0

        emailIds.forEach { emailId ->
            val emailMbox = emailMboxLocalRepository.fetchMboxContentById(emailId + ".mbox")
            emailMbox?.let {
                fileRepository.appendMboxContent(Constants.DIR_EMAIL_PACKAGES, tempFilename, it)
                numberOfEmails++
            }
        }

        val finalFilename = "${packageName}_${currentTimeFormatted}_${numberOfEmails}_${if (isPhishy) "phishy" else "safe"}.mbox"
        val renamedFile = fileRepository.renameFile(Constants.DIR_EMAIL_PACKAGES, tempFilename, finalFilename)
        if (renamedFile != null) {
            Log.d(logTag, "Package file renamed successfully to $finalFilename")
        } else {
            Log.e(logTag, "Failed to rename package file to $finalFilename")
        }


        val fileSize = fileRepository.getFileSizeInBytes(Constants.DIR_EMAIL_PACKAGES, finalFilename)
        val metadata = EmailPackageMetadata(finalFilename, isPhishy, packageName, currentTime, fileSize, numberOfEmails)
        emailPackageManifestManager.addEntryToManifest(metadata)
        Log.d(logTag, "Package metadata added for $finalFilename")

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

    fun loadEmailPackageContent(fileName: String): String? {
        Log.d(logTag, "Loading email package content for $fileName")
        return fileRepository.loadMboxContent(Constants.DIR_EMAIL_PACKAGES, fileName)
    }

    fun deleteEmailPackage(fileName: String) {
        Log.d(logTag, "Deleting email package: $fileName")
        emailPackageManifestManager.removePackageFromManifest(fileName)
        fileRepository.deleteFile(Constants.DIR_EMAIL_PACKAGES, fileName)
        Log.d(logTag, "$fileName deleted successfully")
    }
}
