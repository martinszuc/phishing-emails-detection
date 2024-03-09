package com.martinszuc.phishing_emails_detection.data.email_package

import android.net.Uri
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import java.io.File
import javax.inject.Inject

class EmailPackageRepository @Inject constructor(
    private val emailPackageManager: EmailPackageManager,
    private val packageManifestManager: PackageManifestManager,
    private val fileRepository: FileRepository
) {
    suspend fun createAndSaveEmailPackage(
        emailIds: List<String>,
        isPhishy: Boolean,
        packageName: String
    ): String {
        return emailPackageManager.createEmailPackage(emailIds, isPhishy, packageName)
    }

    fun loadEmailPackagesMetadata(): List<EmailPackageMetadata> {
        // Load and return the list of package metadata from the manifest
        return packageManifestManager.loadManifest()
    }

    fun loadEmailPackageContent(fileName: String): String? {
        return fileRepository.loadMboxContent(fileName = fileName)
    }

    suspend fun deleteEmailPackage(fileName: String) {
        // Remove package from manifest
        packageManifestManager.removePackageFromManifest(fileName)

        // Delete the package file itself
        fileRepository.deleteFile(Constants.DIR_EMAIL_PACKAGES, fileName)
    }

    suspend fun createAndSaveEmailPackageFromMbox(uri: Uri, is_phishy: Boolean, packageName: String): Boolean? {
        return emailPackageManager.createAndSaveEmailPackageFromMbox(uri, is_phishy, packageName)
    }
    fun getEmailPackageMetadataByFilename(fileName: String): EmailPackageMetadata? {
        // Assuming you have a method or a way to fetch all the email packages
        val allPackages = loadEmailPackagesMetadata()
        return allPackages.firstOrNull { it.fileName == fileName }
    }

}