package com.martinszuc.phishing_emails_detection.data.local.repository

import androidx.room.withTransaction
import com.martinszuc.phishing_emails_detection.data.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailFull
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
            emailFullDao.insertEmailFull(emailFull)
        }
    }

    suspend fun getEmailFullById(id: String): EmailFull {
        return emailFullDao.getEmailFullById(id)
    }

    suspend fun getAllEmailsFull(): List<EmailFull> {
        return emailFullDao.getAllEmailsFull()
    }

    suspend fun deleteEmailFullById(id: String) {
        database.withTransaction {
            emailFullDao.deleteEmailFullById(id)
        }
    }
}