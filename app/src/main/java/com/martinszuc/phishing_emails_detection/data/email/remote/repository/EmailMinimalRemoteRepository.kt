package com.martinszuc.phishing_emails_detection.data.email.remote.repository


import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.martinszuc.phishing_emails_detection.data.auth.AccountManager
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.remote.api.GmailApiService
import com.martinszuc.phishing_emails_detection.data.email.remote.api.datasource.GmailPagingSource
import com.martinszuc.phishing_emails_detection.data.email.remote.api.datasource.SearchGmailPagingSource
import com.martinszuc.phishing_emails_detection.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

/**
 * This is a EmailRemoteRepository class that handles emails.
 * It provides methods for retrieving emails from remote data sources.
 *
 * @author matoszuc@gmail.com
 */
class EmailMinimalRemoteRepository @Inject constructor(
    private val apiService: GmailApiService,
    private val accountManager: AccountManager
) {
    fun getEmails(): Flow<PagingData<EmailMinimal>> {
        val account = accountManager.googleAccount.value
        return if (account != null) {
            Pager(
                config = PagingConfig(
                    pageSize = Constants.GMAIL_PAGER_PAGE,
                    initialLoadSize = Constants.GMAIL_PAGER_INIT,
                    prefetchDistance = Constants.GMAIL_PAGER_PREFETCH,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { GmailPagingSource(account, apiService) }
            ).flow
        } else {
            Log.d("EmailMinimalRemoteRepo", "Google SignIn Account is null")
            emptyFlow() // Return an empty flow if no account is found
        }
    }

    fun searchEmails(query: String): Flow<PagingData<EmailMinimal>> {
        val account = accountManager.googleAccount.value
        return if (account != null) {
            Pager(
                config = PagingConfig(
                    pageSize = Constants.GMAIL_PAGER_PAGE,
                    initialLoadSize = Constants.GMAIL_PAGER_INIT,
                    prefetchDistance = Constants.GMAIL_PAGER_PREFETCH,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { SearchGmailPagingSource(account, apiService, query) }
            ).flow
        } else {
            Log.d("EmailMinimalRemoteRepo", "Google SignIn Account is null")
            emptyFlow() // Similarly, return an empty flow for search if no account is found
        }
    }
}