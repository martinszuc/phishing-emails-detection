package com.martinszuc.phising_emails_detection.data

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.martinszuc.phising_emails_detection.network.GmailApiService

class EmailRepository(context: Context, account: GoogleSignInAccount) {

    private val apiService = GmailApiService(context, account)

    suspend fun fetchEmails() = apiService.getEmails()
}
