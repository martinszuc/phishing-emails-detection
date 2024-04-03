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
    private val emailMboxMetadataDao = database.emailMboxDao()

    suspend fun saveEmailMbox(emailId: String, mboxContent: String, timestamp: Long) {
        val filename = "$emailId.mbox"
        try {
            fileRepository.saveMboxContent(mboxContent, Constants.MBOX_FILES_DIR, filename)
            val metadata = EmailMboxMetadata(id = filename, timestamp = timestamp)
            emailMboxMetadataDao.insert(metadata)
            Log.d("EmailMboxRepo", "Saved mbox file and metadata successfully: $filename")
        } catch (e: Exception) {
            Log.e("EmailMboxRepo", "Failed to save mbox file or metadata: $filename", e)
        }
    }

    suspend fun fetchMboxContentById(id: String): String? {
        try {
            val metadata = emailMboxMetadataDao.getEmailMbox(id)
            return metadata?.let {
                fileRepository.loadMboxContent(Constants.MBOX_FILES_DIR, it.id).also {
                    Log.d("EmailMboxRepo", "Fetched mbox content for id: $id")
                }
            } ?: run {
                Log.w("EmailMboxRepo", "No metadata found for id: $id")
                null
            }
        } catch (e: Exception) {
            Log.e("EmailMboxRepo", "Failed to fetch mbox content for id: $id", e)
            return null
        }
    }

    suspend fun deleteEmailMbox(filename: String) {
        try {
            fileRepository.deleteFile(Constants.MBOX_FILES_DIR, filename)
            emailMboxMetadataDao.deleteEmailMbox(filename)
            Log.d("EmailMboxRepo", "Deleted mbox file and metadata: $filename")
        } catch (e: Exception) {
            Log.e("EmailMboxRepo", "Failed to delete mbox file or metadata: $filename", e)
        }
    }

    suspend fun refreshDatabaseFromFiles() {
        try {
            val mboxFiles = fileRepository.listFilesInDirectory(Constants.MBOX_FILES_DIR)
            mboxFiles?.forEach { file ->
                // Assume file naming convention allows extraction of emailId, senderEmail, and timestamp
                // For simplicity, not shown here. Requires parsing the filename or storing this data within the file.
                Log.d("EmailMboxRepo", "Processing file during refresh: ${file.name}")
                // TODO: Implement the logic to parse the filename and store this data within the file.
            }
            Log.d("EmailMboxRepo", "Refreshed database from files successfully")
        } catch (e: Exception) {
            Log.e("EmailMboxRepo", "Failed to refresh database from files", e)
        }
    }
}