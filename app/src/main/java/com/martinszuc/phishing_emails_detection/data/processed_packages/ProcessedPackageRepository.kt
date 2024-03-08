package com.martinszuc.phishing_emails_detection.data.processed_packages

import com.martinszuc.phishing_emails_detection.data.email_package.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import java.io.File
import javax.inject.Inject

class ProcessedPackageRepository @Inject constructor(
    private val processedPackageManifestManager: ProcessedPackageManifestManager,
    private val fileRepository: FileRepository
) {
    fun loadProcessedPackagesMetadata(): List<ProcessedPackageMetadata> =
        processedPackageManifestManager.loadManifest()

    fun loadProcessedPackageContent(fileName: String): String? =
        fileRepository.loadCsvContent(Constants.OUTPUT_CSV_DIR, fileName)

    fun deleteProcessedPackage(fileName: String) {
        processedPackageManifestManager.removePackageFromManifest(fileName)
        fileRepository.deleteCsvFile(Constants.OUTPUT_CSV_DIR, fileName)
    }

}