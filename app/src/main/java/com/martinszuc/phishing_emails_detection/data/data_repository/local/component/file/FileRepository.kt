package com.martinszuc.phishing_emails_detection.data.data_repository.local.component.file

import android.content.Context
import android.net.Uri
import android.util.Log
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import java.io.File
import javax.inject.Inject

private const val logTag = "FileRepository"

/**
 * Serves as a high-level abstraction over the FileManager for managing file operations related
 * to email packages and mbox files. This class simplifies file management tasks by providing
 * methods to save, load, delete, and manipulate mbox content and related metadata.
 *
 * @author matoszuc@gmail.com
 */

class FileRepository @Inject constructor(private val fileManager: FileManager) {

    fun compressAndReturnName(directoryName: String, fileName: String): String {
        val originalFile = fileManager.loadFileFromDirectory(directoryName, fileName)
        originalFile?.let {
            val compressedFileName = "${fileName.split(".")[0]}.gz"
            // Assuming `compressFile` is a method in FileManager that compresses the file and returns the path
            return fileManager.compressFile(directoryName, fileName, compressedFileName)
        } ?: throw Exception("Original file for compression not found: $fileName")
    }

    /**
     * Decompresses a file stored in a specific directory and handles any exceptions or errors.
     * Utilizes FileManager to perform the actual decompression.
     *
     * @param originalFile The file to be decompressed.
     * @return The path of the decompressed file, or the original file path if decompression fails.
     */
    fun decompressFile(originalFile: File): String {
        try {
            val decompressedFile = fileManager.decompressFile(originalFile)
            return decompressedFile.absolutePath
        } catch (e: Exception) {
            Log.e(logTag, "Error decompressing file: ${e.message}")
            return originalFile.absolutePath  // Return the original path if decompression fails
        }
    }

    // Save mbox content to a file within a specified directory
    fun saveMboxContent(
        mboxContent: String,
        directoryName: String = Constants.DIR_EMAIL_PACKAGES,
        fileName: String
    ): File {
        return fileManager.saveTextToFile(mboxContent, directoryName, fileName)
    }
    fun saveTemporaryWeights(weightsContent: ByteArray, fileName: String = "temp_weights.gz"): File {
        val tempFile = fileManager.saveBinaryToFile(weightsContent, "", fileName)
        return tempFile
    }


    // Load a file by name from a specified directory
    fun loadFileFromDirectory(
        directoryName: String = Constants.DIR_EMAIL_PACKAGES,
        fileName: String
    ): File? {
        return fileManager.loadFileFromDirectory(directoryName, fileName)
    }

    // List all files within a specified directory
    fun listFilesInDirectory(directoryName: String = Constants.DIR_EMAIL_PACKAGES): List<File>? {
        return fileManager.listFilesInDirectory(directoryName)
    }

    fun loadFileContent(
        directoryName: String,
        fileName: String
    ): String? {
        return fileManager.readFileContent(directoryName, fileName)
    }

    fun loadFileContent(uri: Uri): String {
        return fileManager.loadFileContentFromUri(uri)
    }

    // Get the size of the file in bytes
    fun getFileSizeInBytes(
        directoryName: String = Constants.DIR_EMAIL_PACKAGES,
        fileName: String
    ): Long {
        val file = fileManager.loadFileFromDirectory(directoryName, fileName)
        return file?.length() ?: 0L
    }

    fun deleteFile(directoryName: String, fileName: String) {
        fileManager.deleteFile(directoryName, fileName)
    }

    fun copyFileFromUri(uri: Uri, directoryName: String, fileName: String): File? {
        return fileManager.copyFileFromUri(uri, directoryName, fileName)
    }

    // In FileRepository.kt
    fun saveTemporaryWeights(weightsContent: String, fileName: String = "temp_weights.json"): File {
        // This will overwrite the file each time it's called
        return fileManager.saveTextToFile(weightsContent, "", fileName)
    }

    fun countEmailsInMbox(file: File): Int {
        return fileManager.countEmailsInMbox(file)
    }

    fun getFilePath(directoryName: String, fileName: String): String? {
        val file = loadFileFromDirectory(directoryName, fileName)
        return file?.absolutePath
    }

    // Load a CSV file content by name from a specified directory
    fun loadCsvContent(
        directoryName: String = Constants.OUTPUT_CSV_DIR,
        fileName: String
    ): String? {
        return fileManager.readFileContent(directoryName, fileName)
    }

    fun clearDirectory(directoryName: String) {
        Log.d(logTag, "Clearing directory: $directoryName")
        fileManager.deleteAllFilesInDirectory(directoryName)
    }

    // Delete a CSV file from a specified directory
    fun deleteCsvFile(directoryName: String = Constants.OUTPUT_CSV_DIR, fileName: String) {
        fileManager.deleteFile(directoryName, fileName)
    }

    fun copyCsvFromUri(
        uri: Uri,
        directoryName: String = Constants.OUTPUT_CSV_DIR,
        fileName: String
    ): File? {
        return copyFileFromUri(uri, directoryName, fileName)
    }

    fun countRowsInCsv(directoryName: String = Constants.OUTPUT_CSV_DIR, fileName: String): Int {
        val csvContent = loadCsvContent(directoryName, fileName) ?: return 0
        return StringUtils.countCsvRowsIgnoringEmptyLines(csvContent) - 1 // Subtract 1 for the header row
    }

    fun renameFile(directoryName: String, currentFileName: String, newFileName: String): File? {
        val currentFile = fileManager.loadFileFromDirectory(directoryName, currentFileName)
        if (currentFile != null && currentFile.exists()) {
            val newFile = File(currentFile.parent, newFileName)
            if (currentFile.renameTo(newFile)) {
                return newFile
            } else {
                // Log or handle the failure to rename the file
            }
        }
        return null
    }

    fun saveMboxForPrediction(
        context: Context,
        mboxContent: String,
        fileName: String = "emails.mbox"
    ): File {
        return fileManager.saveMboxForPrediction(context, mboxContent, fileName)
    }

    fun appendMboxContent(
        directoryName: String = Constants.DIR_EMAIL_PACKAGES,
        fileName: String,
        mboxContent: String
    ) {
        fileManager.appendMboxToFile(directoryName, fileName, mboxContent)
    }
}