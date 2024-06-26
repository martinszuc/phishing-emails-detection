package com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails

import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.martinszuc.phishing_emails_detection.data.data_repository.local.db.AppDatabase
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.Subject
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.emails.EmailFactory
import com.martinszuc.phishing_emails_detection.utils.emails.MboxFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val logTag = "EmailFullLocalRepository"

/**
 * Manages the lifecycle and storage of EmailFull entities within the local database. This repository facilitates
 * CRUD operations on emails, supports complex transactions such as the simultaneous storage of emails and their
 * associated metadata, and provides methods to query and manage emails based on various attributes. It also handles
 * the conversion of emails from EML to mbox format and ensures their persistence.
 *
 * @author matoszuc@gmail.com
 */
class EmailFullLocalRepository @Inject constructor(
    private val database: AppDatabase,
    private val fileRepository: FileRepository,
    private val mboxLocalRepository: EmailMboxLocalRepository
) {
    private val emailFullDao = database.emailFullDao()
    private val subjectDao = database.subjectDao()

    suspend fun insertEmailFull(emailFull: EmailFull) {
        database.withTransaction {
            emailFullDao.insert(emailFull)
        }
    }
    suspend fun saveEmlToEmailFull(uri: Uri) {
        withContext(Dispatchers.IO) {
            val content = fileRepository.loadFileContent(uri) // TODO Read line by line
            val emailFull = EmailFactory.parseEmlToEmailFull(content)

            database.withTransaction {
                // Insert EmailFull
                emailFullDao.insert(emailFull)

                // Convert the EmailFull to EmailMinimal and insert
                val emailMinimal = EmailFactory.createEmailMinimalFromFull(emailFull)
                database.emailMinimalDao().insert(emailMinimal)

                // Convert the EmailFull to MBOX format and save it
                val mboxContent = MboxFactory.formatEmailFullToMbox(emailFull)
                mboxLocalRepository.saveEmailMbox(emailFull.id, mboxContent, emailFull.internalDate)
            }
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
            database.emailMinimalDao().clearAll()
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



    suspend fun deleteEmailById(id: String) {
        database.withTransaction {
            emailFullDao.deleteEmailFullById(id)
        }
    }

}