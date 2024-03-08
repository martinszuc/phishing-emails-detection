package com.martinszuc.phishing_emails_detection.data.file

import android.content.Context
import android.net.Uri
import com.martinszuc.phishing_emails_detection.utils.Constants
import java.io.File
import java.text.DecimalFormat
import javax.inject.Inject

class FileRepository @Inject constructor(private val fileManager: FileManager) {


    // Save mbox content to a file within a specified directory
    fun saveMboxContent(mboxContent: String, directoryName: String = Constants.DIR_EMAIL_PACKAGES, fileName: String): File {
        return fileManager.saveTextToFile(mboxContent, directoryName, fileName)
    }

    // Load a file by name from a specified directory
    fun loadFileFromDirectory(directoryName: String = Constants.DIR_EMAIL_PACKAGES, fileName: String): File? {
        return fileManager.loadFileFromDirectory(directoryName, fileName)
    }

    // List all files within a specified directory
    fun listFilesInDirectory(directoryName: String = Constants.DIR_EMAIL_PACKAGES): List<File>? {
        return fileManager.listFilesInDirectory(directoryName)
    }

    fun loadMboxContent(directoryName: String = Constants.DIR_EMAIL_PACKAGES, fileName: String): String? {
        return fileManager.readFileContent(directoryName, fileName)
    }

    // Get the size of the file in MB
    fun getFileSizeInMB(directoryName: String = Constants.DIR_EMAIL_PACKAGES, fileName: String): Long {
        val file = fileManager.loadFileFromDirectory(directoryName, fileName)
        return file?.length() ?: 0L
    }

    // Get the size of the file in bytes
    fun getFileSizeInBytes(directoryName: String = Constants.DIR_EMAIL_PACKAGES, fileName: String): Long {
        val file = fileManager.loadFileFromDirectory(directoryName, fileName)
        return file?.length() ?: 0L
    }

    fun deleteFile(directoryName: String, fileName: String) {
        fileManager.deleteFile(directoryName, fileName)
    }

    fun copyFileFromUri(uri: Uri, directoryName: String, fileName: String): File? {
        return fileManager.copyFileFromUri(uri, directoryName, fileName)
    }

    fun countEmailsInMbox(file: File): Int {
        return fileManager.countEmailsInMbox(file)
    }
    fun getFilePath(directoryName: String, fileName: String): String? {
        val file = loadFileFromDirectory(directoryName, fileName)
        return file?.absolutePath
    }

    // Load a CSV file content by name from a specified directory
    fun loadCsvContent(directoryName: String = Constants.OUTPUT_CSV_DIR, fileName: String): String? {
        return fileManager.readFileContent(directoryName, fileName)
    }

    // List all CSV files within a specified directory
    fun listCsvFilesInDirectory(directoryName: String = Constants.OUTPUT_CSV_DIR): List<File>? {
        return fileManager.listFilesInDirectory(directoryName)?.filter { it.name.endsWith("-export.csv") }
    }

    // Save processed CSV content to a file within a specified directory
    fun saveCsvContent(csvContent: String, directoryName: String = Constants.OUTPUT_CSV_DIR, fileName: String): File {
        return fileManager.saveTextToFile(csvContent, directoryName, fileName)
    }

    // Delete a CSV file from a specified directory
    fun deleteCsvFile(directoryName: String = Constants.OUTPUT_CSV_DIR, fileName: String) {
        fileManager.deleteFile(directoryName, fileName)
    }
}
