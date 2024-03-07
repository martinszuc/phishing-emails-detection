package com.martinszuc.phishing_emails_detection.data.file

import android.content.Context
import java.io.File

class FileManager(private val context: Context) {

    // Saves text content to a file within a specific directory in the app's internal storage
    fun saveTextToFile(textContent: String, directoryName: String, fileName: String): File {
        val directory = File(context.filesDir, directoryName).apply {
            if (!exists()) mkdirs() // Create directory if it doesn't exist
        }
        val file = File(directory, fileName)
        file.writeText(textContent)
        return file
    }

    // Loads a file from a specified directory by its name
    fun loadFileFromDirectory(directoryName: String, fileName: String): File? {
        val directory = File(context.filesDir, directoryName)
        val file = File(directory, fileName)
        return if (file.exists()) file else null
    }

    // Optional: List all files within a specific directory
    fun listFilesInDirectory(directoryName: String): List<File>? {
        val directory = File(context.filesDir, directoryName)
        return directory.listFiles()?.toList()
    }

    fun readFileContent(directoryName: String, fileName: String): String? {
        val directory = File(context.filesDir, directoryName)
        val file = File(directory, fileName)
        return if (file.exists()) file.readText() else null
    }

    fun deleteFile(directoryName: String, fileName: String) {
        val file = loadFileFromDirectory(directoryName, fileName)
        if (file?.exists() == true) {
            file.delete()
        }
    }

}
