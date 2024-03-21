package com.martinszuc.phishing_emails_detection.data.email.remote.repository

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.auth.AuthenticationRepository
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailBlob
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailFullLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.remote.api.GmailApiService
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.emails.EmailFactory
import com.martinszuc.phishing_emails_detection.utils.emails.EmailUtils
import java.util.UUID
import javax.inject.Inject

class EmailFullRemoteRepository @Inject constructor(
    private val apiService: GmailApiService,
    private val emailBlobLocalRepository: EmailBlobLocalRepository,
    private val emailFullLocalRepository: EmailFullLocalRepository,
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
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

    suspend fun fetchAndSaveEmailsBasedOnFilterAndLimit(query: String, limit: Int) {
        val account = authenticationRepository.getCurrentAccount()
        if (account != null) {
            val emailsAndBlobs = apiService.fetchEmailsAndBlobsBasedOnFilterAndLimit(account, query, limit)

            emailsAndBlobs.forEach { (emailFull, rawBlob) ->
                // Save the full email
                emailFullLocalRepository.insertAllEmailsFull(listOf(emailFull))

                // Convert and save the minimal email
                val emailMinimal = EmailFactory.createEmailMinimalFromFull(emailFull)
                emailMinimalLocalRepository.insert(emailMinimal)

                // Save the raw email blob with the ID from the full email
                if (rawBlob != null) {
                    val emailBlob = EmailBlob(
                        id = emailFull.id,
                        blob = rawBlob,
                        senderEmail = EmailFactory.parseHeader(String(rawBlob, Charsets.UTF_8), "From") ?: Constants.SENDER_UNKNOWN,
                        timestamp = emailFull.internalDate
                    )
                    emailBlobLocalRepository.insert(emailBlob)
                }
            }
        } else {
            Log.e("EmailFullRemoteRepo", "Google SignIn Account is null, aborting fetch and save process")
        }
    }




}
