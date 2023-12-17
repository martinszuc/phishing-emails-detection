package com.martinszuc.phishing_emails_detection.data.local.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailFull

@Dao
interface EmailFullDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(emailFull: EmailFull)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(emails: List<EmailFull>)

    @Query("SELECT * FROM email_full WHERE id = :id")
    suspend fun getEmailFullById(id: String): EmailFull

    @Query("SELECT * FROM email_full")
    fun getAll(): PagingSource<Int, EmailFull>

    @Query("DELETE FROM email_full WHERE id = :id")
    suspend fun deleteEmailFullById(id: String)
}
