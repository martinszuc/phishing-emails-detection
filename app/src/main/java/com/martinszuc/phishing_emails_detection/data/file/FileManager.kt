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
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private const val logTag = "FileManager"

/**
 * Provides low-level file handling operations within the app's internal storage.
 * This class manages basic file operations such as save, load, delete, and compress,
 * directly interacting with the Android file system to perform these tasks.
 *
 * Authored by matoszuc@gmail.com
 */
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

    /**
     * Decompresses a specified gzip (.gz) file and returns the resulting file.
     * This method directly interacts with file streams to efficiently decompress the file contents.
     *
     * @param originalFile The compressed file to be decompressed.
     * @return File The decompressed file object.
     * @throws IOException If an I/O error occurs during decompression.
     */
    fun decompressFile(originalFile: File): File {
        val decompressedFile = File(originalFile.parent, originalFile.name.replace(".gz", ""))
        GZIPInputStream(FileInputStream(originalFile)).use { gzipInputStream ->
            FileOutputStream(decompressedFile).use { fileOutputStream ->
                gzipInputStream.copyTo(fileOutputStream)
            }
        }
        return decompressedFile
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
