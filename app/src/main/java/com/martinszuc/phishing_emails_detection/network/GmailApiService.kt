package com.martinszuc.phishing_emails_detection.network

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory

import com.martinszuc.phishing_emails_detection.data.models.Email

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GmailApiService(private val context: Context, private val account: GoogleSignInAccount) : IEmailApiService {

    private val transport = NetHttpTransport()
    private val jsonFactory = JacksonFactory.getDefaultInstance()

    override suspend fun getEmails(): List<Email> = withContext(Dispatchers.IO) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf("https://www.googleapis.com/auth/gmail.readonly")
        ).setSelectedAccount(account.account)
        val service = com.google.api.services.gmail.Gmail.Builder(transport, jsonFactory, credential)
            .setApplicationName("Phishing emails detection")
            .build()

        val user = "me"
        val listResponse = service.users().messages().list(user).execute()
        val messages = listResponse.messages

        messages.map { message ->
            val email = service.users().messages().get(user, message.id).execute()

            // Parse the Email object from the raw email string
            // might need to handle attachments and other email parts
            Email(
                from = email.payload.headers.find { it.name == "From" }?.value ?: "",
                subject = email.payload.headers.find { it.name == "Subject" }?.value ?: "",
                body = email.snippet,
                isSelected = false
            )
        }
    }
}

