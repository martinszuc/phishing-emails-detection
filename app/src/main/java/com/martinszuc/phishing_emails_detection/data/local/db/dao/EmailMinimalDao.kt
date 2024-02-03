package com.martinszuc.phishing_emails_detection.data.local.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailMinimal

@Dao
interface EmailMinimalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(email: EmailMinimal)
    @Query("SELECT * FROM email_minimal WHERE id = :id")
    suspend fun getEmailById(id: String): EmailMinimal?
    @Query("SELECT * FROM email_minimal WHERE rowId = :id")
    suspend fun getEmailByRowId(id: Int): EmailMinimal?
    @Query("DELETE FROM email_minimal")
    suspend fun deleteAllEmails()
    @Query("SELECT * FROM email_minimal ORDER BY id DESC LIMIT 1")
    suspend fun getLatestEmail(): EmailMinimal?
    @Query("SELECT * FROM email_minimal ORDER BY rowId DESC")
    fun getAllEmails(): PagingSource<Int, EmailMinimal>
    @Query("SELECT * FROM email_minimal WHERE subject LIKE :query OR sender LIKE :query ORDER BY rowId DESC")
    fun searchEmails(query: String): PagingSource<Int, EmailMinimal>
    @Query("UPDATE email_minimal SET sender = :sender, subject = :subject, body = :body, timestamp = :timestamp, isPhishing = :isPhishing WHERE id = :id")
    suspend fun updateEmail(id: String, sender: String, subject: String, body: String, timestamp: Long, isPhishing: Boolean?)
    @Query("DELETE FROM email_minimal")
    suspend fun clearAll()

    @Transaction
    suspend fun upsert(email: EmailMinimal) {
        val existingEmail = getEmailById(email.id)
        if (existingEmail != null) {
            updateEmail(email.id, email.sender, email.subject, email.body, email.timestamp, email.isPhishing)
        } else {
            insert(email)
        }
    }

    @Transaction
    suspend fun insertAll(emails: List<EmailMinimal>) {
        emails.forEach { email ->
            upsert(email)
        }
    }


}
