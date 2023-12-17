package com.martinszuc.phishing_emails_detection.data.remote.api

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.gmail.Gmail
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailFull
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
import javax.inject.Inject


class GmailApiService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userManager: UserManager
) {
    // TODO import full format of the email when downloading to db.
    private val transport = NetHttpTransport()
    private val jsonFactory = JacksonFactory.getDefaultInstance()

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
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(Constants.GMAIL_READONLY_SCOPE)
            ).setSelectedAccount(userManager.account.value?.account)

            val service = Gmail.Builder(transport, jsonFactory, credential)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build()

            val user = "me"

            val emails = emailIds.map { emailId ->
                val email =
                    service.users().messages().get(user, emailId).setFormat("full").execute()

                val headers = email.payload.headers.map { header ->
                    Header(name = header.name, value = header.value)
                }

                val parts = email.payload.parts.map { part ->
                    val partHeaders = part.headers.map { header ->
                        Header(name = header.name, value = header.value)
                    }
                    val partBody = Body(data = part.body.data, size = part.body.size)
                    Part(
                        partId = part.partId,
                        mimeType = part.mimeType,
                        filename = part.filename,
                        headers = partHeaders,
                        body = partBody
                    )
                }

                val payloadBody =
                    Body(data = email.payload.body.data, size = email.payload.body.size)
                val payload = Payload(
                    partId = email.payload.partId,
                    mimeType = email.payload.mimeType,
                    filename = email.payload.filename,
                    headers = headers,
                    body = payloadBody,
                    parts = parts
                )

                EmailFull(
                    id = email.id,
                    threadId = email.threadId,
                    labelIds = email.labelIds,
                    snippet = email.snippet,
                    historyId = email.historyId.toLong(),
                    internalDate = email.internalDate,
                    payload = payload
                )
            }

            emails
        }
}