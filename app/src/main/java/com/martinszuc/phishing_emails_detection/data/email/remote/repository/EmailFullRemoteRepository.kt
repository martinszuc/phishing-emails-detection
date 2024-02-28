package com.martinszuc.phishing_emails_detection.data.email.remote.repository

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.auth.AuthenticationRepository
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailBlob
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.remote.api.GmailApiService
import javax.inject.Inject

class EmailFullRemoteRepository @Inject constructor(
    private val apiService: GmailApiService,
    private val emailBlobLocalRepository: EmailBlobLocalRepository,
    private val authenticationRepository: AuthenticationRepository
) {
    suspend fun getEmailsFullByIds(emailIds: List<String>): List<EmailFull> {
        val account = authenticationRepository.getCurrentAccount()
        return if (account != null) {
            apiService.fetchEmailFullByIds(emailIds, account)
        } else {
            Log.d("EmailFullRemoteRepository", "Google SignIn Account is null")
            emptyList()
        }
    }

    suspend fun fetchAndSaveRawEmail(emailId: String) {
        val account = authenticationRepository.getCurrentAccount()
        if (account != null) {
            val emailDetails = apiService.fetchRawEmail(emailId, account)
            if (emailDetails != null) {
                val (rawEmail, senderEmail, timestamp) = emailDetails
                rawEmail?.let {
                    val emailBlob = EmailBlob(
                        id = emailId,
                        blob = it,
                        senderEmail = senderEmail,
                        timestamp = timestamp
                    )
                    emailBlobLocalRepository.insert(emailBlob)
                } ?: Log.d("fetchAndSaveRawEmail", "Raw email content is null for ID $emailId")
            } else {
                Log.d("fetchAndSaveRawEmail", "Failed to fetch raw email with ID $emailId")
            }
        } else {
            Log.d("EmailFullRemoteRepository", "Google SignIn Account is null")
        }
    }
}
