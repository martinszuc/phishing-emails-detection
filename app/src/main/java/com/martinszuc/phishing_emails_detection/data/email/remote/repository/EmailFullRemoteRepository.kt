package com.martinszuc.phishing_emails_detection.data.email.remote.repository

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.martinszuc.phishing_emails_detection.data.auth.AuthenticationRepository
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.email.remote.api.GmailApiService
import javax.inject.Inject

/**
 * Handles remote operations for retrieving full email details from Gmail API.
 * This repository interacts with GmailApiService to fetch complete email data,
 * providing support for operations based on email IDs, search queries, and pagination limits.
 *
 * @author matoszuc@gmail.com
 */
private const val logTag = "EmailFullRemoteRepository"

class EmailFullRemoteRepository @Inject constructor(
    private val apiService: GmailApiService,
    private val authenticationRepository: AuthenticationRepository
) {
    /**
     * Retrieves a list of EmailFull objects by their IDs using the Gmail API.
     * Requires authentication state from AuthenticationRepository.
     *
     * @param emailIds List of email IDs to fetch.
     * @return List of EmailFull details or an empty list if errors occur.
     */
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

    /**
     * Fetches emails based on a specified query and limit from the Gmail API.
     * Utilizes progress reporting through a callback function.
     *
     * @param account GoogleSignInAccount used for authentication.
     * @param query Gmail search query string.
     * @param limit Maximum number of emails to fetch.
     * @param progressCallback Callback to report fetch progress.
     * @return List of EmailFull details or an empty list if errors occur.
     */
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
}
