package com.martinszuc.phishing_emails_detection.data.email.local.repository

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.email.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMboxMetadata
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.emails.MboxFactory
import javax.inject.Inject

/**
 * Facilitates storage and management of mbox files and their metadata locally. This repository is responsible
 * for saving and retrieving mbox file content and metadata, deleting mbox files, and converting EmailFull
 * objects to mbox format for storage. It interacts with both the file system and the local database to ensure
 * consistency between stored files and their recorded metadata.
 *
 * @author matoszuc@gmail.com
 */
private const val logTag = "EmailMboxRepository"

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
            Log.d(logTag, "Saved mbox file and metadata successfully: $filename")
        } catch (e: Exception) {
            Log.e(logTag, "Failed to save mbox file or metadata: $filename", e)
        }
    }

    suspend fun buildAndSaveMbox(emailFull: EmailFull) {
        try {
            val mboxContent = MboxFactory.formatEmailFullToMbox(emailFull)
            saveEmailMbox(emailFull.id, mboxContent, emailFull.internalDate)
            Log.d(logTag, "Built and saved mbox for email ID: ${emailFull.id}")
        } catch (e: Exception) {
            Log.e(logTag, "Failed to build and save mbox for email ID: ${emailFull.id}", e)
        }
    }

    suspend fun fetchMboxContentById(id: String): String? {
        try {
            // Attempt to fetch metadata for the mbox file
            val metadata = emailMboxMetadataDao.getEmailMbox(id)
            metadata?.let {
                // Try to load content from the main directory if metadata is found
                fileRepository.loadFileContent(Constants.MBOX_FILES_DIR, it.id)?.let { content ->
                    Log.d(logTag, "Fetched mbox content for id: $id from ${Constants.MBOX_FILES_DIR}")
                    return content // If content is found, return it
                } ?: Log.w(logTag, "No file found in main directory for id: $id, trying fallback")
            }

            // If the main directory attempt fails or metadata is null, try the fallback directory
            return fileRepository.loadFileContent(Constants.SAVED_EMAILS_DIR, id)?.also {
                Log.d(logTag, "Fetched fallback mbox content for id: $id from ${Constants.SAVED_EMAILS_DIR}")
            } ?: run {
                Log.w(logTag, "No file found in fallback directory for id: $id")
                null // Return null if no file is found in the fallback directory
            }
        } catch (e: Exception) {
            Log.e(logTag, "Failed to fetch mbox content for id: $id", e)
            return null // Return null if an exception occurs
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
}