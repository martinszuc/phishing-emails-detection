package com.martinszuc.phishing_emails_detection.data.email.local.repository

import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.martinszuc.phishing_emails_detection.data.email.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailDetection
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.emails.EmailFactory
import com.martinszuc.phishing_emails_detection.utils.emails.MboxFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EmailDetectionLocalRepository @Inject constructor(
    private val database: AppDatabase,
    private val fileRepository: FileRepository
) {
    private val emailDetectionDao = database.emailDetectionDao()

    suspend fun insertEmailDetection(emailDetection: EmailDetection) {
        database.withTransaction {
            emailDetectionDao.insert(emailDetection)
        }
    }

    suspend fun getEmailDetectionById(id: String): EmailDetection? {
        return emailDetectionDao.getEmailDetectionById(id)
    }

    suspend fun deleteEmailDetection(emailDetection: EmailDetection) {
        database.withTransaction {
            emailDetectionDao.delete(emailDetection)
        }
    }

    suspend fun updateEmailDetection(emailDetection: EmailDetection) {
        database.withTransaction {
            emailDetectionDao.update(emailDetection)
        }
    }

    fun getAllEmailDetectionsFlow(): Flow<PagingData<EmailDetection>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { emailDetectionDao.getAllEmailDetectionsPaged() }
        ).flow
    }

    suspend fun processEmlFileToEmailDetection(uri: Uri, isPhishing: Boolean) {
        withContext(Dispatchers.IO) {
            val content = fileRepository.loadFileContent(uri)
            val emailFull = EmailFactory.parseEmlToEmailFull(content)
                ?: throw IllegalArgumentException("Failed to parse the EML file")

            database.withTransaction {

                database.emailFullDao().insert(emailFull)

                // Create and insert an EmailDetection object based on the EmailFull data
                val emailDetection = EmailDetection(emailFull.id ,emailFull, isPhishing)
                insertEmailDetection(emailDetection)

                // Convert the EmailFull to EmailMinimal and insert
                val emailMinimal = EmailFactory.createEmailMinimalFromFull(emailFull)
                database.emailMinimalDao().insert(emailMinimal)
            }

            // Convert the EmailFull to MBOX format and save it
            val mboxContent = MboxFactory.formatEmailFullToMbox(emailFull)
            val mboxFileName = "email-${emailFull.id}.mbox"

            fileRepository.saveMboxContent(mboxContent, Constants.SAVED_EMAILS_DIR, mboxFileName)
        }
    }

    suspend fun clearAll() {
        database.withTransaction {
            emailDetectionDao.clearAll()
        }
    }
}