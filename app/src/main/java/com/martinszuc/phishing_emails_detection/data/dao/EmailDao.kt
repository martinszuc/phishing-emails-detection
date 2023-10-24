package com.martinszuc.phishing_emails_detection.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinszuc.phishing_emails_detection.data.entity.Email

@Dao
interface EmailDao {
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
    //    @Query("SELECT * FROM emails WHERE subject LIKE :query")
//    suspend fun searchEmails(query: String): List<Email>

//    @Query("SELECT * FROM emails ORDER BY rowId ASC")
//    fun getEmails(): PagingSource<Int, Email>
}