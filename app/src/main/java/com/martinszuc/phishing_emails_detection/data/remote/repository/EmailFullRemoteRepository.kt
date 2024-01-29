package com.martinszuc.phishing_emails_detection.data.remote.repository

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailBlob
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.remote.api.GmailApiService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
class EmailFullRemoteRepository @Inject constructor(
    private val apiService: GmailApiService,
    private val emailBlobLocalRepository: EmailBlobLocalRepository
) {
    suspend fun getEmailsFullByIds(emailIds: List<String>): List<EmailFull> {
        return apiService.fetchEmailFullByIds(emailIds)
    }
    suspend fun fetchAndSaveRawEmail(emailId: String) {
        val emailDetails = apiService.fetchRawEmail(emailId)
        if (emailDetails != null) {
            val (rawEmail, senderEmail, timestamp) = emailDetails
            if (rawEmail != null) {  // Check if rawEmail is not null
                val emailBlob = EmailBlob(
                    id = emailId,
                    blob = rawEmail,  // rawEmail is not null here
                    senderEmail = senderEmail,
                    timestamp = timestamp,
                )
                emailBlobLocalRepository.insert(emailBlob)
            } else {
                Log.d("fetchAndSaveRawEmail", "Raw email content is null for ID $emailId")
            }
        } else {
            Log.d("fetchAndSaveRawEmail", "Failed to fetch raw email with ID $emailId")
        }
    }
}
