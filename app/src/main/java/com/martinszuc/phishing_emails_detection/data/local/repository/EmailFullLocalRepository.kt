package com.martinszuc.phishing_emails_detection.data.local.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.martinszuc.phishing_emails_detection.data.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.EmailFull
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
class EmailFullLocalRepository@Inject constructor(
    private val database: AppDatabase
) {
    private val emailFullDao = database.emailFullDao()

    suspend fun insertEmailFull(emailFull: EmailFull) {
        database.withTransaction {
            emailFullDao.insert(emailFull)
        }
    }

    suspend fun insertAllEmailsFull(emails: List<EmailFull>) {
        database.withTransaction {
            emailFullDao.insertAll(emails)
        }
    }

    suspend fun getEmailFullById(id: String): EmailFull {
        return emailFullDao.getEmailFullById(id)
    }

    fun getAllEmailsFull(): Flow<PagingData<EmailFull>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { emailFullDao.getAll() }
        ).flow
    }

    suspend fun deleteEmailFullById(id: String) {
        database.withTransaction {
            emailFullDao.deleteEmailFullById(id)
        }
    }
}