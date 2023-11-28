package com.martinszuc.phishing_emails_detection.data.local.repository

import androidx.room.withTransaction
import com.martinszuc.phishing_emails_detection.data.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.local.entity.Email
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
}