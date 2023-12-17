package com.martinszuc.phishing_emails_detection.data.remote.repository

import com.martinszuc.phishing_emails_detection.data.local.entity.EmailFull
import com.martinszuc.phishing_emails_detection.data.remote.api.GmailApiService
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
class EmailFullRemoteRepository @Inject constructor(
    private val apiService: GmailApiService
) {
    suspend fun getEmailsFullByIds(emailIds: List<String>): List<EmailFull> {
        return apiService.fetchEmailFullByIds(emailIds)
    }
}
