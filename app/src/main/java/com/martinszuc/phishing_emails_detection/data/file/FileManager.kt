package com.martinszuc.phishing_emails_detection.data.file

import android.content.Context
import android.net.Uri
import com.martinszuc.phishing_emails_detection.utils.Constants
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.zip.GZIPOutputStream

private const val logTag = "FileManager"


class FileManager(private val context: Context) {

    fun getAppDirectory(): File {
        // Assuming files are stored in the app's internal storage directory
        return context.filesDir
    }

    fun saveBinaryToFile(content: ByteArray, directoryName: String, fileName: String): File {
        val directory = File(getAppDirectory(), directoryName)
        if (!directory.exists()) directory.mkdirs()

        val file = File(directory, fileName)
        file.writeBytes(content)
        return file
    }

    fun compressFile(directoryName: String, originalFileName: String, compressedFileName: String): String {
        val originalFile = File(context.filesDir, directoryName + File.separator + originalFileName)
        val compressedFile = File(context.filesDir, directoryName + File.separator + compressedFileName)

        GZIPOutputStream(FileOutputStream(compressedFile)).use { output ->
            FileInputStream(originalFile).use { input ->
                input.copyTo(output)
            }
        }

        return compressedFile.name
    }

    fun saveTextToFile(textContent: String, directoryName: String, fileName: String): File {
        val directory = File(context.filesDir, directoryName)
        if (!directory.exists()) directory.mkdirs()

        val file = File(directory, fileName)
        file.writeText(textContent)
        return file
    }

    fun appendMboxToFile(directoryName: String, fileName: String, text: String) {
        val dir = File(context.filesDir, directoryName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, fileName)
        file.appendText("$text\n\n")
    }

    suspend fun saveTextToFileAndGetFileSize(textContent: String, directoryName: String, fileName: String): Long {
        val directory = File(context.filesDir, directoryName).apply {
            if (!exists()) mkdirs() // Create directory if it doesn't exist
        }
        val file = File(directory, fileName)

        // Write the text content to the file
        file.writeText(textContent)

        // Return the size of the file
        // Using a new File instance to ensure we're getting updated file metadata
        return File(file.absolutePath).length()
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

    // Method to get all directories within the app's internal files folder
    fun getAllDirectories(): List<File> {
        val filesDir = context.filesDir
        return filesDir.listFiles()?.filter { it.isDirectory }.orEmpty()
    }

    // Method to remove a directory by name in the app's internal files folder
    fun removeDirectory(directoryName: String): Boolean {
        val directory = File(context.filesDir, directoryName)
        return if (directory.exists() && directory.isDirectory) {
            directory.deleteRecursively()
        } else false
    }

    fun saveMboxForPrediction(context: Context, mboxContent: String, fileName: String = "emails.mbox"): File {
        val directory = File(context.filesDir, Constants.PREDICTION_MBOX_DIR).apply {
            if (!exists()) mkdirs() // Create directory if it doesn't exist
        }

        val file = File(directory, fileName)
        file.writeText(mboxContent)
        return file
    }

    // In FileManager.kt
    fun deleteAllFilesInDirectory(directoryName: String) {
        val directory = File(context.filesDir, directoryName)
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.delete()
                }
            }
        }
    }

    fun loadFileContentFromUri(uri: Uri): String {
        val contentResolver = context.contentResolver
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                return reader.readText()
            }
        }
        throw FileNotFoundException("Unable to open file from URI: $uri")
    }


}
