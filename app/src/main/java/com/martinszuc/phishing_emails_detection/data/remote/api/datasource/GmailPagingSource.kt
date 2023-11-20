package com.martinszuc.phishing_emails_detection.data.remote.api.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.martinszuc.phishing_emails_detection.data.local.entity.Email
import com.martinszuc.phishing_emails_detection.data.remote.api.GmailApiService

/**
 * This is a GmailPagingSource class that extends PagingSource from the Android Paging library.
 * It fetches emails from a Gmail API service and provides them to a Pager.
 *
 * @property apiService The GmailApiService instance for fetching emails.
 *
 * @author matoszuc@gmail.com
 */
class GmailPagingSource(
    private val apiService: GmailApiService
) : PagingSource<String, Email>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Email> {
        return try {
            val response = apiService.getEmails(params.key, params.loadSize)
            LoadResult.Page(
                data = response.first,
                prevKey = null, // Gmail API only supports forward pagination.
                nextKey = response.second
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Email>): String? {
        return null
    }
}
