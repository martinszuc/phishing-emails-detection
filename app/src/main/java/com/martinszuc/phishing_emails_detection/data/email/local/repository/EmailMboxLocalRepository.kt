package com.martinszuc.phishing_emails_detection.data.email.local.repository

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.email.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMboxMetadata
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.emails.MboxFactory
import javax.inject.Inject

private const val logTag = "EmailMboxRepo"

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
            val metadata = emailMboxMetadataDao.getEmailMbox(id)
            // Attempt to load content from the main directory if metadata is found
            metadata?.let {
                return fileRepository.loadFileContent(Constants.MBOX_FILES_DIR, it.id).also {
                    Log.d(logTag, "Fetched mbox content for id: $id from ${Constants.MBOX_FILES_DIR}")
                }
            }
            // If metadata is not found, attempt to load from the fallback directory
            return try {
                fileRepository.loadFileContent(Constants.SAVED_EMAILS_DIR, id).also {
                    Log.d(logTag, "Fetched fallback mbox content for id: $id from ${Constants.SAVED_EMAILS_DIR}")
                }
            } catch (e: Exception) {
                Log.w(logTag, "No file found in fallback directory for id: $id")
                null // If still not found, return null
            }
        } catch (e: Exception) {
            Log.e(logTag, "Failed to fetch mbox content for id: $id", e)
            return null // Log the exception and return null if any other error occurs
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