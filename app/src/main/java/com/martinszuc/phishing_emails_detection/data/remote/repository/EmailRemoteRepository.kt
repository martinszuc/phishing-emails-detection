package com.martinszuc.phishing_emails_detection.data.remote.repository


import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.data.local.entity.Email
import com.martinszuc.phishing_emails_detection.data.remote.api.datasource.GmailPagingSource
import com.martinszuc.phishing_emails_detection.data.remote.api.datasource.SearchGmailPagingSource
import com.martinszuc.phishing_emails_detection.data.remote.api.GmailApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * This is a EmailRemoteRepository class that handles emails.
 * It provides methods for retrieving emails from remote data sources.
 *
 * @author matoszuc@gmail.com
 */
class EmailRemoteRepository @Inject constructor(
    private val apiService: GmailApiService
) {
    fun getEmails(): Flow<PagingData<Email>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.GMAIL_PAGER_PAGE,
                initialLoadSize = Constants.GMAIL_PAGER_INIT,
                prefetchDistance = Constants.GMAIL_PAGER_PREFETCH,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { GmailPagingSource(apiService) }
        ).flow
    }

    fun searchEmails(query: String): Flow<PagingData<Email>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.GMAIL_PAGER_PAGE,
                initialLoadSize = Constants.GMAIL_PAGER_INIT,
                prefetchDistance = Constants.GMAIL_PAGER_PREFETCH,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { SearchGmailPagingSource(apiService, query) }
        ).flow
    }
}
