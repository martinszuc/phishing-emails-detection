package com.martinszuc.phishing_emails_detection.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailFull

@Dao
interface EmailFullDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmailFull(emailFull: EmailFull)

    @Query("SELECT * FROM email_full WHERE id = :id")
    suspend fun getEmailFullById(id: String): EmailFull

    @Query("SELECT * FROM email_full")
    suspend fun getAllEmailsFull(): List<EmailFull>

    @Query("DELETE FROM email_full WHERE id = :id")
    suspend fun deleteEmailFullById(id: String)
}
