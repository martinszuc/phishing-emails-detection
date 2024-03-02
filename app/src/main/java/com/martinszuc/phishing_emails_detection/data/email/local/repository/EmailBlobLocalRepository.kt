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
        return EmailUtils.formatToMbox(emailBlob)
    }

    suspend fun deleteEmailBlob(id: String) {
        database.withTransaction {
            emailBlobDao.deleteEmailBlob(id)
        }
    }

    suspend fun getMboxesByIds(ids: List<String>): List<String> {
        return ids.mapNotNull { id ->
            emailBlobDao.getEmailBlob(id)?.let { EmailUtils.formatToMbox(it) }
        }
    }

}
