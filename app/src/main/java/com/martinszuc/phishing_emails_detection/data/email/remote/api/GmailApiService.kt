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
import com.martinszuc.phishing_emails_detection.utils.factory.EmailFactory
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