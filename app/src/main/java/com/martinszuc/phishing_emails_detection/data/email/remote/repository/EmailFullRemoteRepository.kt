package com.martinszuc.phishing_emails_detection.data.email.remote.repository

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.martinszuc.phishing_emails_detection.data.auth.AuthenticationRepository
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailBlob
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailFullLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailMboxLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.remote.api.GmailApiService
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.emails.EmailFactory
import com.martinszuc.phishing_emails_detection.utils.emails.EmailUtils
import javax.inject.Inject

private const val logTag = "EmailFullRemoteRepo"

class EmailFullRemoteRepository @Inject constructor(
    private val apiService: GmailApiService,
    private val emailBlobLocalRepository: EmailBlobLocalRepository,
    private val emailMboxLocalRepository: EmailMboxLocalRepository,
    private val emailFullLocalRepository: EmailFullLocalRepository,
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
    private val authenticationRepository: AuthenticationRepository
) {

    suspend fun getEmailsFullByIds(emailIds: List<String>): List<EmailFull> {
        Log.d(logTag, "Starting to fetch full emails for IDs: $emailIds")
        val account = authenticationRepository.getCurrentAccount()
        if (account == null) {
            Log.d(logTag, "Google SignIn Account retrieval failed; account is null.")
            return emptyList()
        }
        try {
            val emails = apiService.fetchEmailFullByIds(emailIds, account)
            Log.d(logTag, "Successfully fetched ${emails.size} emails for IDs: $emailIds")
            return emails
        } catch (e: Exception) {
            Log.e(logTag, "Failed to fetch emails for IDs $emailIds: ${e.localizedMessage}")
            return emptyList()
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
                    val mboxString = EmailUtils.FormatBlobToMbox(emailBlob)

//                    emailBlobLocalRepository.insert(emailBlob) // TODO
                    emailMboxLocalRepository.saveEmailMbox(emailId, mboxString, timestamp)

                } ?: Log.d(logTag, "Raw email content is null for ID $emailId")
            } else {
                Log.d(logTag, "Failed to fetch raw email with ID $emailId")
            }
        } else {
            Log.d(logTag, "Google SignIn Account is null")
        }
    }

    suspend fun fetchEmailsBasedOnFilterAndLimit(
        account: GoogleSignInAccount,
        query: String,
        limit: Int,
        progressCallback: (Int) -> Unit
    ): List<EmailFull> {
        Log.d(logTag, "Fetching emails with query '$query' and limit $limit")
        return try {
            val emails = apiService.fetchFullEmailsBasedOnFilterAndLimit(account, query, limit, progressCallback)
            Log.d(logTag, "Fetched ${emails.size} emails based on the filter and limit.")
            emails
        } catch (e: Exception) {
            Log.e(logTag, "Error during fetch with query '$query': ${e.localizedMessage}")
            emptyList()
        }
    }

    private suspend fun saveRawEmailData(emailFull: EmailFull, rawBlob: ByteArray) {
        val emailBlob = EmailBlob(
            id = emailFull.id,
            blob = rawBlob,
            senderEmail = EmailFactory.parseHeader(String(rawBlob, Charsets.UTF_8), "From") ?: Constants.SENDER_UNKNOWN,
            timestamp = emailFull.internalDate
        )
        val mboxString = EmailUtils.FormatBlobToMbox(emailBlob)
        emailMboxLocalRepository.saveEmailMbox(emailFull.id, mboxString, emailFull.internalDate)
    }


}
