package com.martinszuc.phishing_emails_detection.data.remote.repository

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailBlob
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.remote.api.GmailApiService
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
        val rawEmail = apiService.fetchRawEmail(emailId)
        if (rawEmail != null) {
            val emailBlob = EmailBlob(id = emailId, blob = rawEmail)
            emailBlobLocalRepository.insert(emailBlob)
        } else {
            Log.d("fetchAndSaveRawEmail", "Failed to fetch raw email with ID $emailId")
        }
    }
}
