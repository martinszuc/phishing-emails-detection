package com.martinszuc.phishing_emails_detection.data.email.local.repository

import androidx.room.withTransaction
import com.martinszuc.phishing_emails_detection.data.email.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailBlob
import com.martinszuc.phishing_emails_detection.utils.emails.EmailUtils
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
class EmailBlobLocalRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val emailBlobDao = database.emailBlobDao()

    suspend fun insert(emailBlob: EmailBlob) {
        database.withTransaction {
            emailBlobDao.insert(emailBlob)
        }
    }

    suspend fun getMboxById(id: String): String {
        val emailBlob = emailBlobDao.getEmailBlob(id)
        return EmailUtils.FormatBlobToMbox(emailBlob)
    }

    suspend fun getBlobById(id: String): EmailBlob? {
        return emailBlobDao.getEmailBlob(id)
    }

    suspend fun deleteEmailBlob(id: String) {
        database.withTransaction {
            emailBlobDao.deleteEmailBlob(id)
        }
    }
}
