package com.martinszuc.phishing_emails_detection.data

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.martinszuc.phishing_emails_detection.data.models.Email
import com.martinszuc.phishing_emails_detection.network.GmailPagingSource
import com.martinszuc.phishing_emails_detection.network.GmailApiService
import kotlinx.coroutines.flow.Flow


private const val MY_PAGE_SIZE = 13

class EmailRepository(context: Context, account: GoogleSignInAccount) {

    private val apiService = GmailApiService(context, account)

    fun fetchEmails(): Flow<PagingData<Email>> {
        return Pager(
            config = PagingConfig(
                pageSize = MY_PAGE_SIZE, // Replace with your page size
                enablePlaceholders = false
            ),
            pagingSourceFactory = { GmailPagingSource(apiService) }
        ).flow
    }
}