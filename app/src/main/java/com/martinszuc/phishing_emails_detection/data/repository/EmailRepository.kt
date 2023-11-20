package com.martinszuc.phishing_emails_detection.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.martinszuc.phishing_emails_detection.data.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.entity.Email
import com.martinszuc.phishing_emails_detection.data.api.EmailPagingSource
import com.martinszuc.phishing_emails_detection.data.api.SearchEmailPagingSource
import com.martinszuc.phishing_emails_detection.data.api.GmailApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
class EmailRepository @Inject constructor(
    private val database: AppDatabase,
    private val apiService: GmailApiService
) {
    companion object {
        private const val PAGE_SIZE = 11
    }
    private val emailDao = database.emailDao()

    fun getEmails(): Flow<PagingData<Email>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { EmailPagingSource(apiService) }
        ).flow
    }

    fun searchEmails(query: String): Flow<PagingData<Email>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { SearchEmailPagingSource(apiService, query) }
        ).flow
    }

    suspend fun insertAll(emails: List<Email>) {
        database.withTransaction {
            emailDao.insertAll(emails)
        }
    }
}
