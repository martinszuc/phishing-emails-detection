package com.martinszuc.phishing_emails_detection.data.email_package

import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import java.io.File
import javax.inject.Inject

class EmailPackageRepository @Inject constructor(
    private val emailPackageManager: EmailPackageManager,
    private val packageManifestManager: PackageManifestManager,
    private val fileRepository: FileRepository
) {
    suspend fun createAndSaveEmailPackage(emailIds: List<String>, isPhishy: Boolean): String {
        return emailPackageManager.createEmailPackage(emailIds, isPhishy)
    }

    fun loadEmailPackagesMetadata(): List<EmailPackageMetadata> {
        // Load and return the list of package metadata from the manifest
        return packageManifestManager.loadManifest()
    }

    fun loadEmailPackageContent(fileName: String): String? {
        // Assuming all packages are saved under a standard directory ("emailPackages")
        return fileRepository.loadMboxContent(fileName = fileName)
    }

}