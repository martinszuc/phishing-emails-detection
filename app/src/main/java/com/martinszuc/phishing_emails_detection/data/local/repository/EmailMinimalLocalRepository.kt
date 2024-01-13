package com.martinszuc.phishing_emails_detection.data.local.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.martinszuc.phishing_emails_detection.data.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailMinimal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


/**
 * This is a EmailLocalRepository class that handles emails.
 * It provides methods for communication with local database.
 *
 * @author matoszuc@gmail.com
 */
class EmailMinimalLocalRepository@Inject constructor(
    private val database: AppDatabase
) {
    private val emailMinimalDao = database.emailMinimalDao()

    suspend fun insertAll(emails: List<EmailMinimal>) {
        database.withTransaction {
            emailMinimalDao.insertAll(emails)
        }
    }

    suspend fun insert(email: EmailMinimal) {
        database.withTransaction {
            emailMinimalDao.insert(email)
        }
    }

    fun getAllEmails(): Flow<PagingData<EmailMinimal>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { emailMinimalDao.getAllEmails() }).flow
    }
    fun getAllEmailsForDetector(): Flow<PagingData<EmailMinimal>> {
        return Pager(
            config = PagingConfig(pageSize = 4, enablePlaceholders = false),
            pagingSourceFactory = { emailMinimalDao.getAllEmails() }).flow
    }

    fun searchEmails(query: String): Flow<PagingData<EmailMinimal>> {
        return Pager(PagingConfig(pageSize = 10)) {
            emailMinimalDao.searchEmails("%$query%")
        }.flow
    }
}

