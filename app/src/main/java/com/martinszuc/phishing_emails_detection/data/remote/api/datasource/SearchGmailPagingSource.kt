package com.martinszuc.phishing_emails_detection.data.remote.api.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.remote.api.GmailApiService
/**
 * This is a SearchGmailPagingSource class that extends PagingSource from the Android Paging library.
 * It fetches emails from a Gmail API service based on a search query and provides them to a Pager.
 *
 * @property apiService The GmailApiService instance for fetching emails.
 * @property query The search query to use when fetching emails.
 *
 * @author matoszuc@gmail.com
 */
class SearchGmailPagingSource(
    private val apiService: GmailApiService,
    private val query: String
) : PagingSource<String, EmailMinimal>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, EmailMinimal> {
        return try {
            val response = apiService.searchEmailsMinimal(query, params.key, params.loadSize)
            LoadResult.Page(
                data = response.first,
                prevKey = null, // Gmail API only supports forward pagination.
                nextKey = response.second
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, EmailMinimal>): String? {
        return null
    }
}