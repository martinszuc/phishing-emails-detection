package com.martinszuc.phishing_emails_detection.data.file

import android.content.Context
import android.net.Uri
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

    fun copyFileFromUri(uri: Uri, directoryName: String, fileName: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val directory = File(context.filesDir, directoryName).apply {
                if (!exists()) mkdirs()
            }
            val file = File(directory, fileName)
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun countEmailsInMbox(file: File): Int {
        var emailCount = 0
        try {
            file.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.startsWith("From ")) { // Assuming each email starts with "From "
                        emailCount++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emailCount
    }

}
