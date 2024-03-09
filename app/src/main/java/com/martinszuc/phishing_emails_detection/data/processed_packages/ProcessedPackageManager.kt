package com.martinszuc.phishing_emails_detection.data.processed_packages

import com.martinszuc.phishing_emails_detection.data.email_package.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import java.io.File
import javax.inject.Inject

class ProcessedPackageManager @Inject constructor(
    private val processedPackageManifestManager: ProcessedPackageManifestManager,
    private val fileRepository: FileRepository
) {
    suspend fun refreshProcessedPackages() {
        val outputCsvDirPath = fileRepository.getFilePath("", Constants.OUTPUT_CSV_DIR) ?: return
        val assetsCsvSamplesPath = fileRepository.getFilePath("", Constants.CSV_SAMPLES_DIR) ?: return
        val outputCsvDir = File(outputCsvDirPath)
        val assetsCsvSamplesDir = File(assetsCsvSamplesPath)

        processedPackageManifestManager.refreshManifestFromDirectory(outputCsvDir)
//        processedPackageManifestManager.refreshManifestFromDirectory(assetsCsvSamplesDir)             // TODO load csv
    }
}
