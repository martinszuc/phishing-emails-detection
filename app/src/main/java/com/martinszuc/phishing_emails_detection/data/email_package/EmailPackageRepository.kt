package com.martinszuc.phishing_emails_detection.data.email_package

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
    suspend fun createAndSaveEmailPackage(emailIds: List<String>, isPhishy: Boolean, packageName: String): String {
        return emailPackageManager.createEmailPackage(emailIds, isPhishy, packageName)
    }

    fun loadEmailPackagesMetadata(): List<EmailPackageMetadata> {
        // Load and return the list of package metadata from the manifest
        return packageManifestManager.loadManifest()
    }

    fun loadEmailPackageContent(fileName: String): String? {
        // Assuming all packages are saved under a standard directory ("emailPackages")
        return fileRepository.loadMboxContent(fileName = fileName)
    }

    suspend fun deleteEmailPackage(fileName: String) {
        // Remove package from manifest
        packageManifestManager.removePackageFromManifest(fileName)

        // Delete the package file itself
        fileRepository.deleteFile(Constants.DIR_EMAIL_PACKAGES, fileName)
    }

}