package com.martinszuc.phishing_emails_detection.data.email.remote.api

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.ListMessagesResponse
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.emails.EmailFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Base64
import javax.inject.Inject
import kotlin.math.min


class GmailApiService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val transport = NetHttpTransport()
    private val jsonFactory = JacksonFactory.getDefaultInstance()

    suspend fun getEmailsMinimal(
        account: GoogleSignInAccount,
        pageToken: String?,
        pageSize: Int
    ): Pair<List<EmailMinimal>, String?> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(Constants.GMAIL_READONLY_SCOPE)
            ).setSelectedAccount(account.account)

            val service = Gmail.Builder(transport, jsonFactory, credential)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build()

            val listResponse = fetchEmailList(service, null, pageToken, pageSize)
            val emails = listResponse.messages?.mapNotNull { message ->
                fetchMessageMinimal(service, "me", message.id)
            } ?: emptyList()

            Pair(emails, listResponse.nextPageToken)
        } catch (e: UserRecoverableAuthIOException) {
            throw e
        } catch (e: Exception) {
            Log.e("GmailApiService", "An error occurred while fetching emails: ${e.message}")
            throw e
        }
    }

    suspend fun searchEmailsMinimal(
        account: GoogleSignInAccount,
        query: String,
        pageToken: String?,
        pageSize: Int
    ): Pair<List<EmailMinimal>, String?> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(Constants.GMAIL_READONLY_SCOPE)
            ).setSelectedAccount(account.account)
            val service = Gmail.Builder(transport, jsonFactory, credential)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build()

            // Use fetchEmailMinimal method to reuse the fetching and mapping logic
            val listResponse = fetchEmailList(service, query, pageToken, pageSize)
            val emails = listResponse.messages?.mapNotNull { message ->
                fetchMessageMinimal(service, "me", message.id)
            } ?: emptyList()

            Pair(emails, listResponse.nextPageToken)
        } catch (e: Exception) {
            Log.e("GmailApiService", "An error occurred while searching emails: ${e.message}")
            throw e
        }
    }

    private fun fetchEmailList(
        service: Gmail,
        query: String?,
        pageToken: String?,
        pageSize: Int
    ): ListMessagesResponse {
        val user = "me"
        return service.users().messages().list(user)
            .setMaxResults(pageSize.toLong())
            .setPageToken(pageToken)
            .apply { if (!query.isNullOrEmpty()) q = query }
            .execute()
    }

    private fun fetchMessageMinimal(
        service: Gmail,
        user: String,
        messageId: String
    ): EmailMinimal? {
        val email = service.users().messages().get(user, messageId).execute()
        return EmailFactory.createEmailMinimal(email)
    }

    suspend fun fetchEmailFullByIds(
        emailIds: List<String>,
        account: GoogleSignInAccount
    ): List<EmailFull> =
        withContext(Dispatchers.IO) {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(Constants.GMAIL_READONLY_SCOPE)
            ).setSelectedAccount(account.account)

            val service = Gmail.Builder(transport, jsonFactory, credential)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build()

            emailIds.mapNotNull { emailId ->
                val email = service.users().messages().get("me", emailId).setFormat("full").execute()
                EmailFactory.createEmailFull(email)
            }
        }


    suspend fun fetchRawEmail(
        emailId: String,
        account: GoogleSignInAccount
    ): Triple<ByteArray?, String, Long>? = withContext(Dispatchers.IO) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(Constants.GMAIL_READONLY_SCOPE)
        ).setSelectedAccount(account.account)

        val service = Gmail.Builder(transport, jsonFactory, credential)
            .setApplicationName(Constants.APPLICATION_NAME)
            .build()

        val email = service.users().messages().get("me", emailId).setFormat("raw").execute()
        val rawEmail = email.raw?.let { Base64.decodeBase64(it) }

        if (rawEmail != null) {
            val emailContent = String(rawEmail, Charsets.UTF_8)
            val senderEmail = EmailFactory.parseHeader(emailContent, "From") ?: Constants.SENDER_UNKNOWN
            val timestamp = email.internalDate ?: 0L
            Triple(rawEmail, senderEmail, timestamp)
        } else {
            Log.d("GmailApiService", "Raw email content is null for ID: $emailId")
            null
        }
    }


    suspend fun fetchFullEmailsBasedOnFilterAndLimit(
        account: GoogleSignInAccount,
        query: String,
        limit: Int
    ): List<EmailFull> = withContext(Dispatchers.IO) {
        val emailFullList = mutableListOf<EmailFull>()
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(Constants.GMAIL_READONLY_SCOPE)
            ).setSelectedAccount(account.account)

            val service = Gmail.Builder(transport, jsonFactory, credential)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build()

            var pageToken: String? = null
            var emailsFetched = 0

            while (emailsFetched < limit) {
                val response = service.users().messages().list("me")
                    .setQ(query)
                    .setMaxResults(min(limit - emailsFetched, 100).toLong()) // Batch size per request
                    .setPageToken(pageToken)
                    .execute()

                val messages = response.messages ?: break // Exit if no more messages

                messages.forEach { message ->
                    val emailId = message.id
                    val email = service.users().messages().get("me", emailId).setFormat("full").execute()
                    EmailFactory.createEmailFull(email)?.let {
                        emailFullList.add(it)
                        emailsFetched++
                    }

                    // Optionally, handle raw email content here as needed
                }

                pageToken = response.nextPageToken ?: break // Exit if at the last page
            }
        } catch (e: Exception) {
            Log.e("GmailApiService", "Error fetching emails: ${e.message}")
            // Handle exceptions appropriately
        }
        return@withContext emailFullList
    }


    suspend fun fetchEmailsAndBlobsBasedOnFilterAndLimit(
        account: GoogleSignInAccount,
        query: String,
        limit: Int
    ): List<Pair<EmailFull, ByteArray?>> = withContext(Dispatchers.IO) {
        val emailsAndBlobsList = mutableListOf<Pair<EmailFull, ByteArray?>>()
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(Constants.GMAIL_READONLY_SCOPE)
            ).setSelectedAccount(account.account)

            val service = Gmail.Builder(transport, jsonFactory, credential)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build()

            var pageToken: String? = null
            var emailsFetched = 0

            while (emailsFetched < limit) {
                val response = service.users().messages().list("me")
                    .setQ(query)
                    .setMaxResults(min(limit - emailsFetched, 100).toLong())
                    .setPageToken(pageToken)
                    .execute()

                val messages = response.messages ?: break

                messages.forEach { message ->
                    val emailId = message.id
                    val fullEmail = service.users().messages().get("me", emailId).setFormat("full").execute()
                    val rawEmail = service.users().messages().get("me", emailId).setFormat("raw").execute().raw?.let { Base64.decodeBase64(it) }

                    EmailFactory.createEmailFull(fullEmail)?.let {
                        emailsAndBlobsList.add(it to rawEmail)
                        emailsFetched++
                    }
                }

                pageToken = response.nextPageToken ?: break
            }
        } catch (e: Exception) {
            Log.e("GmailApiService", "Error fetching emails and blobs: ${e.message}")
        }
        return@withContext emailsAndBlobsList
    }




}