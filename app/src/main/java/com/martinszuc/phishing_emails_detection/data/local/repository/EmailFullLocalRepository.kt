package com.martinszuc.phishing_emails_detection.data.local.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.martinszuc.phishing_emails_detection.data.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.local.entity.Subject
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.EmailFull
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
class EmailFullLocalRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val emailFullDao = database.emailFullDao()
    private val subjectDao = database.subjectDao()

    suspend fun insertEmailFull(emailFull: EmailFull) {
        database.withTransaction {
            emailFullDao.insert(emailFull)
        }
    }

    suspend fun insertAllEmailsFull(emails: List<EmailFull>) {
        emails.forEach { email ->
            // Insert the EmailFull object into the EmailFull table
            emailFullDao.insert(email)

            // Extract the subject from the headers
            val subjectHeader = email.payload.headers.find { it.name == "Subject" }
            if (subjectHeader != null) {
                val subject = Subject(
                    id=0,
                    emailId = email.id,
                    value = subjectHeader.value
                )
                // Insert the subject into the Subject table
                subjectDao.insert(subject)
            }
        }
    }

    suspend fun clearAll() {
        database.withTransaction {
            emailFullDao.clearAll()
            subjectDao.clearAll()
        }
    }

    suspend fun getEmailById(emailId: String): EmailFull? {
        return emailFullDao.getEmailById(emailId)
    }

    fun getAllEmailsFull(): Flow<PagingData<EmailFull>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { emailFullDao.getAll() }
        ).flow
    }

    suspend fun searchEmails(query: String): Flow<PagingData<EmailFull>> {
        val emailIds = subjectDao.searchIdsBySubject(query)
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { emailFullDao.getEmailsByIds(emailIds) }
        ).flow
    }


    suspend fun deleteEmailFullById(id: String) {
        database.withTransaction {
            emailFullDao.deleteEmailFullById(id)
        }
    }
}