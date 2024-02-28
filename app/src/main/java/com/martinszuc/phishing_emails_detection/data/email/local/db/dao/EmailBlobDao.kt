package com.martinszuc.phishing_emails_detection.data.email.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailBlob

@Dao
interface EmailBlobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(emailBlob: EmailBlob)

    @Query("SELECT * FROM email_blob WHERE id = :id")
    suspend fun getEmailBlob(id: String): EmailBlob

    @Query("DELETE FROM email_blob WHERE id = :id")
    suspend fun deleteEmailBlob(id: String)
    @Query("DELETE FROM email_blob")
    suspend fun clearAll()
}
