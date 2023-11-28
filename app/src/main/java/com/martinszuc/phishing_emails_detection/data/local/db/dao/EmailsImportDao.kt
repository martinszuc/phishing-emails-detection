package com.martinszuc.phishing_emails_detection.data.local.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinszuc.phishing_emails_detection.data.local.entity.Email

@Dao
interface EmailsImportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(emails: List<Email>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(email: Email)
    @Query("SELECT * FROM emails WHERE id = :id")
    suspend fun getEmailById(id: String): Email?
    @Query("SELECT * FROM emails WHERE rowId = :id")
    suspend fun getEmailByRowId(id: Int): Email?
    @Query("DELETE FROM emails")
    suspend fun deleteAllEmails()
    @Query("SELECT * FROM emails ORDER BY id DESC LIMIT 1")
    suspend fun getLatestEmail(): Email?
    @Query("SELECT * FROM emails ORDER BY rowId DESC")
    fun getAllEmails(): PagingSource<Int, Email>
    @Query("SELECT * FROM emails WHERE subject LIKE :query OR sender LIKE :query ORDER BY rowId DESC")
    fun searchEmails(query: String): PagingSource<Int, Email>
}
