package com.martinszuc.phishing_emails_detection.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.martinszuc.phishing_emails_detection.data.models.Email

class GmailPagingSource(
    private val apiService: GmailApiService
) : PagingSource<Int, Email>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Email> {
        return try {
            val nextPageNumber = params.key ?: 1
            val response = apiService.getEmails(nextPageNumber, params.loadSize)
            val nextKey = if (response.size < params.loadSize) null else nextPageNumber + 1
            LoadResult.Page(
                data = response,
                prevKey = if (nextPageNumber > 1) nextPageNumber - 1 else null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Email>): Int? {
        return state.anchorPosition
    }
}

