package com.martinszuc.phishing_emails_detection.network

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory

import com.martinszuc.phishing_emails_detection.data.entity.Email

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val HTTPS_WWW_GOOGLEAPIS_COM_AUTH_GMAIL_READONLY = "https://www.googleapis.com/auth/gmail.readonly"

private const val APPLICATION_NAME = "Phishing emails detection"
class GmailApiService(private val context: Context, private val account: GoogleSignInAccount) {

    private val transport = NetHttpTransport()
    private val jsonFactory = JacksonFactory.getDefaultInstance()

    suspend fun getEmails(pageToken: String?, pageSize: Int): Pair<List<Email>, String?> = withContext(Dispatchers.IO) {
        Log.d("GmailApiService", "Fetching emails with pageToken: $pageToken and pageSize: $pageSize")
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(HTTPS_WWW_GOOGLEAPIS_COM_AUTH_GMAIL_READONLY)
        ).setSelectedAccount(account.account)
        val service = com.google.api.services.gmail.Gmail.Builder(transport, jsonFactory, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()

        val user = "me"
        val listResponse = service.users().messages().list(user).setMaxResults(pageSize.toLong()).setPageToken(pageToken).execute()
        val messages = listResponse.messages
        Log.d("GmailApiService", "Page token: $pageToken, Next page token: ${listResponse.nextPageToken}")

        val emails = messages.map { message ->
            val email = service.users().messages().get(user, message.id).execute()

            Email(
                id = message.id,
                from = email.payload.headers.find { it.name == "From" }?.value ?: "",
                subject = email.payload.headers.find { it.name == "Subject" }?.value ?: "",
                body = email.snippet,
                isSelected = false
            )
        }

        Log.d("GmailApiService", "Number of emails fetched: ${emails.size}")
        Pair(emails, listResponse.nextPageToken)
    }




    suspend fun searchEmails(query: String, pageToken: String?, pageSize: Int): Pair<List<Email>, String?> = withContext(Dispatchers.IO) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(HTTPS_WWW_GOOGLEAPIS_COM_AUTH_GMAIL_READONLY)
        ).setSelectedAccount(account.account)
        val service = com.google.api.services.gmail.Gmail.Builder(transport, jsonFactory, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()

        val user = "me"
        val listResponse = service.users().messages().list(user).setQ("subject:$query").setMaxResults(pageSize.toLong()).setPageToken(pageToken).execute()
        val messages = listResponse.messages

        val emails = messages.map { message ->
            val email = service.users().messages().get(user, message.id).execute()

            Email(
                id = message.id,
                from = email.payload.headers.find { it.name == "From" }?.value ?: "",
                subject = email.payload.headers.find { it.name == "Subject" }?.value ?: "",
                body = email.snippet,
                isSelected = false
            )
        }

        Pair(emails, listResponse.nextPageToken)
    }

}
