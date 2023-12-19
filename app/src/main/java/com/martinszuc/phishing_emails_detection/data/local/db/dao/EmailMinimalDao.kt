package com.martinszuc.phishing_emails_detection.data.local.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailMinimal

@Dao
interface EmailMinimalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(emails: List<EmailMinimal>)
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
}
