package com.martinszuc.phishing_emails_detection.data.email.local.repository

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.email.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMboxMetadata
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import javax.inject.Inject


class EmailMboxLocalRepository @Inject constructor(
    private val database: AppDatabase,
    private val fileRepository: FileRepository
) {
    private val logTag = "EmailMboxRepo"
    private val emailMboxMetadataDao = database.emailMboxDao()

    suspend fun saveEmailMbox(emailId: String, mboxContent: String, timestamp: Long) {
        val filename = "$emailId.mbox"
        try {
            fileRepository.saveMboxContent(mboxContent, Constants.MBOX_FILES_DIR, filename)
            val metadata = EmailMboxMetadata(id = filename, timestamp = timestamp)
            emailMboxMetadataDao.insert(metadata)
            Log.d(logTag, "Saved mbox file and metadata successfully: $filename")
        } catch (e: Exception) {
            Log.e(logTag, "Failed to save mbox file or metadata: $filename", e)
        }
    }

    suspend fun fetchMboxContentById(id: String): String? {
        try {
            val metadata = emailMboxMetadataDao.getEmailMbox(id)
            return metadata.let {
                fileRepository.loadMboxContent(Constants.MBOX_FILES_DIR, it.id).also {
                    Log.d(logTag, "Fetched mbox content for id: $id")
                }
            } ?: run {
                Log.w(logTag, "No metadata found for id: $id")
                null
            }
        } catch (e: Exception) {
            Log.e(logTag, "Failed to fetch mbox content for id: $id", e)
            return null
        }
    }

    suspend fun deleteEmailMboxById(emailId: String) {
        val filename = "$emailId.mbox" // Construct the filename from the email ID
        try {
            // Attempt to delete the file from the directory
            fileRepository.deleteFile(Constants.MBOX_FILES_DIR, filename)
            // Attempt to delete the corresponding metadata from the database
            emailMboxMetadataDao.deleteEmailMbox(filename) // Ensure this method accepts the filename as the identifier
            Log.d(logTag, "Deleted mbox file and metadata: $filename")
        } catch (e: Exception) {
            Log.e(logTag, "Failed to delete mbox file or metadata: $filename", e)
        }
    }

    suspend fun refreshDatabaseFromFiles() {
        try {
            val mboxFiles = fileRepository.listFilesInDirectory(Constants.MBOX_FILES_DIR)
            mboxFiles?.forEach { file ->
                // Assume file naming convention allows extraction of emailId, senderEmail, and timestamp
                // For simplicity, not shown here. Requires parsing the filename or storing this data within the file.
                Log.d(logTag, "Processing file during refresh: ${file.name}")
                // TODO: Implement the logic to parse the filename and store this data within the file.
            }
            Log.d(logTag, "Refreshed database from files successfully")
        } catch (e: Exception) {
            Log.e(logTag, "Failed to refresh database from files", e)
        }
    }
}