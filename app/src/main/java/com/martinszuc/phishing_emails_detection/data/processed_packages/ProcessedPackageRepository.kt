package com.martinszuc.phishing_emails_detection.data.processed_packages

import android.net.Uri
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.data.processed_packages.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import java.io.File
import javax.inject.Inject

class ProcessedPackageRepository @Inject constructor(
    private val processedPackageManifestManager: ProcessedPackageManifestManager,
    private val fileRepository: FileRepository
) {
    suspend fun refreshProcessedPackages() {
        val outputCsvDirPath = fileRepository.getFilePath("", Constants.OUTPUT_CSV_DIR) ?: return
        val outputCsvDir = File(outputCsvDirPath)
        processedPackageManifestManager.refreshManifestFromDirectory(outputCsvDir)
    }

    fun createAndAddProcessedPackageFromCsv(uri: Uri, isPhishy: Boolean, packageName: String) {
        val tempFileName = "temp_$packageName.csv"
        val copiedFile = fileRepository.copyCsvFromUri(uri, Constants.OUTPUT_CSV_DIR, tempFileName)

        if (copiedFile != null) {
            val numberOfRows = fileRepository.countRowsInCsv(
                Constants.OUTPUT_CSV_DIR,
                tempFileName
            ) - 1 // Subtract 1 for the header row
            val creationDate = System.currentTimeMillis()
            val formattedDate = StringUtils.formatTimestampForFilename(creationDate)
            val phishyStatus = if (isPhishy) "phishy" else "safe"
            val fileName =
                "${packageName}_${formattedDate}_${numberOfRows}_${phishyStatus}-export.csv"

            val finalFile =
                fileRepository.renameFile(Constants.OUTPUT_CSV_DIR, tempFileName, fileName)
            if (finalFile != null) {
                val fileSize = finalFile.length()
                val metadata = ProcessedPackageMetadata(
                    fileName, isPhishy, packageName, creationDate, fileSize, numberOfRows
                )
                processedPackageManifestManager.addPackageToManifest(metadata)
            }
        }
    }

    fun loadProcessedPackagesMetadata(): List<ProcessedPackageMetadata> =
        processedPackageManifestManager.refreshManifest()

    fun deleteProcessedPackage(fileName: String) {
        processedPackageManifestManager.removePackageFromManifest(fileName)
        fileRepository.deleteCsvFile(Constants.OUTPUT_CSV_DIR, fileName)
    }

}