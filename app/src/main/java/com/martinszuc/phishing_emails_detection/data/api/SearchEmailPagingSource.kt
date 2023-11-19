package com.martinszuc.phishing_emails_detection.data.api

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.martinszuc.phishing_emails_detection.data.entity.Email

class SearchEmailPagingSource(
    private val apiService: GmailApiService,
    private val query: String
) : PagingSource<String, Email>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Email> {
        return try {
            val response = apiService.searchEmails(query, params.key, params.loadSize)
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