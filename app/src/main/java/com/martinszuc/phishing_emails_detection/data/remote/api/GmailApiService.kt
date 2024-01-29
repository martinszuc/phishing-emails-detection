package com.martinszuc.phishing_emails_detection.data.remote.api

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.MessagePart
import com.google.api.services.gmail.model.MessagePartHeader
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.Body
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.Header
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.Part
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.Payload
import com.martinszuc.phishing_emails_detection.data.remote.UserManager
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Base64
import javax.inject.Inject


class GmailApiService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userManager: UserManager
) {
    private val transport = NetHttpTransport()
    private val jsonFactory = JacksonFactory.getDefaultInstance()

    suspend fun getEmailsMinimal(
        pageToken: String?,
        pageSize: Int
    ): Pair<List<EmailMinimal>, String?> {
        Log.d(
            "GmailApiService",
            "Fetching emails with pageToken: $pageToken and pageSize: $pageSize"
        )
        return fetchEmailMinimal(null, pageToken, pageSize)
    }

    suspend fun searchEmailsMinimal(
        query: String,
        pageToken: String?,
        pageSize: Int
    ): Pair<List<EmailMinimal>, String?> {
        Log.d(
            "GmailApiService",
            "Searching emails with query: $query, pageToken: $pageToken and pageSize: $pageSize"
        )
        return fetchEmailMinimal(query, pageToken, pageSize)
    }

    suspend fun fetchEmailFullByIds(emailIds: List<String>): List<EmailFull> =
        withContext(Dispatchers.IO) {
            val account = userManager.account.value?.account
            if (account == null) {
                Log.d("fetchEmailFullByIds", "Account is null")
                return@withContext emptyList<EmailFull>()
            }

            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(Constants.GMAIL_READONLY_SCOPE)
            ).setSelectedAccount(account)

            val service = Gmail.Builder(transport, jsonFactory, credential)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build()

            val user = "me"

            emailIds.mapNotNull { emailId ->
                fetchFullEmail(service, user, emailId)
            }.also { emails ->
                Log.d("fetchEmailFullByIds", "Fetched ${emails.size} full emails")
            }
        }

    private suspend fun fetchEmailMinimal(
        query: String?,
        pageToken: String?,
        pageSize: Int
    ): Pair<List<EmailMinimal>, String?> =
        withContext(Dispatchers.IO) {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(Constants.GMAIL_READONLY_SCOPE)
            ).setSelectedAccount(userManager.account.value?.account)

            val service = Gmail.Builder(transport, jsonFactory, credential)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build()

            val user = "me"
            val listRequest = service.users().messages().list(user)
                .setMaxResults(pageSize.toLong())
                .setPageToken(pageToken)

            if (query != null) {
                listRequest.q = "in:anywhere (subject:$query OR from:$query)"
            } else {
                listRequest.q = "in:anywhere"
            }

            val listResponse = listRequest.execute()
            val messages = listResponse.messages

            val emails = messages.map { message ->
                val email = service.users().messages().get(user, message.id).execute()

                EmailMinimal(
                    id = message.id,
                    sender = email.payload.headers.find { it.name == "From" }?.value ?: "",
                    subject = email.payload.headers.find { it.name == "Subject" }?.value ?: "",
                    body = email.snippet,
                    timestamp = email.internalDate
                )
            }

            Pair(emails, listResponse.nextPageToken)
        }

    private fun fetchFullEmail(service: Gmail, user: String, emailId: String): EmailFull? {
        val email = service.users().messages().get(user, emailId).setFormat("full").execute()
        val payload = email?.payload ?: run {
            Log.d("fetchEmail", "Email with ID $emailId has null payload")
            return null
        }

        val headers = createHeaders(payload.headers)
        val parts = createParts(payload.parts)
        val payloadBody = payload.body?.let { Body(data = it.data ?: "", size = it.size) } ?: run {
            Log.d("fetchEmail", "Email with ID $emailId has null payload body")
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


    suspend fun fetchRawEmail(emailId: String): Triple<ByteArray?, String, Long>? {
        val account = userManager.account.value?.account
        if (account == null) {
            Log.d("fetchRawEmail", "Account is null")
            return null
        }

        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(Constants.GMAIL_READONLY_SCOPE)
        ).setSelectedAccount(account)

        val service = Gmail.Builder(transport, jsonFactory, credential)
            .setApplicationName(Constants.APPLICATION_NAME)
            .build()

        val user = "me"

        val email = service.users().messages().get(user, emailId).setFormat("raw").execute()
        val rawEmail = email?.raw?.let { Base64.decodeBase64(it) }
        if (rawEmail != null) {
            // Convert the raw email from ByteArray to String
            val emailContent = String(rawEmail, Charsets.UTF_8)
            // Parse the headers from the email content string
            val senderEmail = parseHeader(emailContent, "From") ?: "unknown@sender.com"
            val timestamp = email.internalDate ?: 0L
            return Triple(rawEmail, senderEmail, timestamp)
        } else {
            Log.d("fetchRawEmail", "Raw email content is null for ID: $emailId")
            return null
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