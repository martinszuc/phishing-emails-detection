package com.martinszuc.phishing_emails_detection.data.file

import android.content.Context
import java.io.File
import javax.inject.Inject

class FileRepository @Inject constructor(private val fileManager: FileManager) {

    // Define a default directory name for mbox files or allow specifying it
    private val defaultMboxDirectory = "mboxFiles"

    // Save mbox content to a file within a specified directory
    fun saveMboxContent(mboxContent: String, directoryName: String = defaultMboxDirectory, fileName: String): File {
        return fileManager.saveTextToFile(mboxContent, directoryName, fileName)
    }

    // Load a file by name from a specified directory
    fun loadFileFromDirectory(directoryName: String = defaultMboxDirectory, fileName: String): File? {
        return fileManager.loadFileFromDirectory(directoryName, fileName)
    }

    // List all files within a specified directory
    fun listFilesInDirectory(directoryName: String = defaultMboxDirectory): List<File>? {
        return fileManager.listFilesInDirectory(directoryName)
    }

    fun loadMboxContent(directoryName: String = "emailPackages", fileName: String): String? {
        return fileManager.readFileContent(directoryName, fileName)
    }
}
