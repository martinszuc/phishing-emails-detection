package com.martinszuc.phishing_emails_detection.data.local.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.martinszuc.phishing_emails_detection.data.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.local.entity.Email
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


/**
 * This is a EmailLocalRepository class that handles emails.
 * It provides methods for communication with local database.
 *
 * @author matoszuc@gmail.com
 */
class EmailLocalRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val emailsImportDao = database.emailsImportDao()

    suspend fun insertAll(emails: List<Email>) {
        database.withTransaction {
            emailsImportDao.insertAll(emails)
        }
    }

    suspend fun insert(email: Email) {
        database.withTransaction {
            emailsImportDao.insert(email)
        }
    }

    fun getAllEmails(): Flow<PagingData<Email>> {
        return Pager(PagingConfig(pageSize = 10)) {
            emailsImportDao.getAllEmails()
        }.flow
    }

    fun searchEmails(query: String): Flow<PagingData<Email>> {
        return Pager(PagingConfig(pageSize = 10)) {
            emailsImportDao.searchEmails("%$query%")
        }.flow
    }
}

