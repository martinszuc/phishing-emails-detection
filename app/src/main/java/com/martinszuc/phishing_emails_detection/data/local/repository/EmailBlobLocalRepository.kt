package com.martinszuc.phishing_emails_detection.data.local.repository

import androidx.room.withTransaction
import com.martinszuc.phishing_emails_detection.data.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailBlob
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
class EmailBlobLocalRepository@Inject constructor(
        private val database: AppDatabase
    ) {
        private val emailBlobDao = database.emailBlobDao()

        suspend fun insert(emailBlob: EmailBlob) {
            database.withTransaction {
                emailBlobDao.insert(emailBlob)
            }
        }

        suspend fun getEmailBlob(id: String): EmailBlob {
            return emailBlobDao.getEmailBlob(id)
        }

        suspend fun deleteEmailBlob(id: String) {
            database.withTransaction {
                emailBlobDao.deleteEmailBlob(id)
            }
        }
    }
