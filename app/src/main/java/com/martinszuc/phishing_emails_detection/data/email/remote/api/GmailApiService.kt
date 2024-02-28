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
import com.google.api.services.gmail.model.MessagePart
import com.google.api.services.gmail.model.MessagePartHeader
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.Body
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.Header
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.Part
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.Payload
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Base64
import javax.inject.Inject


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

            fetchEmailMinimal(service, null, pageToken, pageSize) // Pass service directly
        } catch (e: UserRecoverableAuthIOException) {
            // Specific handling or rethrowing for UserRecoverableAuthIOException
            throw e
        } catch (e: Exception) {
            // General exception handling
            Log.e("GmailApiService", "An error occurred while fetching emails: ${e.message}")
            throw e // Rethrow or handle the general exception as needed
        }
    }

    suspend fun searchEmailsMinimal(
        account: GoogleSignInAccount,
        query: String,
        pageToken: String?,
        pageSize: Int
    ): Pair<List<EmailMinimal>, String?> = withContext(Dispatchers.IO) {
        Log.d(
            "GmailApiService",
            "Searching emails with query: $query, pageToken: $pageToken and pageSize: $pageSize"
        )
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(Constants.GMAIL_READONLY_SCOPE)
        ).setSelectedAccount(account.account)

        val service = Gmail.Builder(transport, jsonFactory, credential)
            .setApplicationName(Constants.APPLICATION_NAME)
            .build()

        fetchEmailMinimal(service, query, pageToken, pageSize) // Corrected to pass the service
    }

    private suspend fun fetchEmailMinimal(
        service: Gmail, // Accept Gmail service directly
        query: String?,
        pageToken: String?,
        pageSize: Int
    ): Pair<List<EmailMinimal>, String?> = withContext(Dispatchers.IO) {
        val listResponse = fetchEmailList(service, query, pageToken, pageSize)
        val emails = listResponse.messages?.mapNotNull { message ->
            fetchMessageMinimal(service, "me", message.id)
        } ?: emptyList()

        Pair(emails, listResponse.nextPageToken)
    }

    private fun fetchEmailList(
        service: Gmail,
        query: String?,
        pageToken: String?,
        pageSize: Int
    ): ListMessagesResponse {
        val user = "me"
        val listRequest = service.users().messages().list(user)
            .setMaxResults(pageSize.toLong())
            .setPageToken(pageToken)

        if (!query.isNullOrEmpty()) {
            listRequest.q = query
        }

        return listRequest.execute()
    }

    private fun fetchMessageMinimal(
        service: Gmail,
        user: String,
        messageId: String
    ): EmailMinimal? {
        val email = service.users().messages().get(user, messageId).execute()

        return EmailMinimal(
            id = email.id,
            sender = email.payload.headers.find { it.name == "From" }?.value ?: "",
            subject = email.payload.headers.find { it.name == "Subject" }?.value ?: "",
            body = email.snippet,
            timestamp = email.internalDate
        )
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
                fetchFullEmail(service, "me", emailId)
            }.also { emails ->
                Log.d("GmailApiService", "Fetched ${emails.size} full emails")
            }
        }

    private fun fetchFullEmail(service: Gmail, user: String, emailId: String): EmailFull? {
        val email = service.users().messages().get(user, emailId).setFormat("full").execute()
        val payload = email.payload ?: run {
            Log.d("GmailApiService", "Email with ID $emailId has null payload")
            return null
        }

        val headers = createHeaders(payload.headers)
        val parts = createParts(payload.parts)
        val payloadBody = payload.body?.let { Body(data = it.data ?: "", size = it.size) } ?: run {
            Log.d("GmailApiService", "Email with ID $emailId has null payload body")
            return null
        }

        val newPayload = Payload(
            partId = payload.partId,
            mimeType = payload.mimeType,
            filename = payload.filename,
            headers = headers,
            body = payloadBody,
            parts = parts
        )

        return EmailFull(
            id = email.id,
            threadId = email.threadId,
            labelIds = email.labelIds,
            snippet = email.snippet,
            historyId = email.historyId.toLong(),
            internalDate = email.internalDate,
            payload = newPayload
        )
    }

    private fun createHeaders(headers: List<MessagePartHeader>?): List<Header> {
        return headers?.map { header ->
            Header(name = header.name, value = header.value)
        } ?: emptyList()
    }

    private fun createParts(parts: List<MessagePart>?): List<Part> {
        return parts?.mapNotNull { part ->
            val partHeaders = createHeaders(part.headers)
            val partBody = part.body?.let { Body(data = it.data ?: "", size = it.size ?: 0) }
            if (partBody != null) {
                Part(
                    partId = part.partId,
                    mimeType = part.mimeType,
                    filename = part.filename,
                    headers = partHeaders,
                    body = partBody
                )
            } else {
                null
            }
        } ?: emptyList()
    }


    suspend fun fetchRawEmail(
        emailId: String,
        account: GoogleSignInAccount
    ): Triple<ByteArray?, String, Long>? =
        withContext(Dispatchers.IO) {
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
                val senderEmail = parseHeader(emailContent, "From") ?: "unknown@sender.com"
                val timestamp = email.internalDate ?: 0L
                Triple(rawEmail, senderEmail, timestamp)
            } else {
                Log.d("GmailApiService", "Raw email content is null for ID: $emailId")
                null
            }
        }

    private fun parseHeader(emailContent: String, headerName: String): String? {
        val lines = emailContent.split("\r\n")
        for (line in lines) {
            if (line.startsWith(headerName)) {
                return line.substringAfter(": ").trim()
            }
        }
        return null
    }
}