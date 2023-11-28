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
    private val emailDao = database.emailDao()

    suspend fun insertAll(emails: List<Email>) {
        database.withTransaction {
            emailDao.insertAll(emails)
        }
    }

    suspend fun insert(email: Email) {
        database.withTransaction {
            emailDao.insert(email)
        }
    }

    fun getAllEmails(): Flow<PagingData<Email>> {
        return Pager(PagingConfig(pageSize = 10)) {
            emailDao.getAllEmails()
        }.flow
    }

    fun searchEmails(query: String): Flow<PagingData<Email>> {
        return Pager(PagingConfig(pageSize = 10)) {
            emailDao.searchEmails("%$query%")
        }.flow
    }
}

