package com.martinszuc.phishing_emails_detection.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailBlob

@Dao
interface EmailBlobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(emailBlob: EmailBlob)

    @Query("SELECT * FROM emails_blob WHERE id = :id")
    suspend fun getEmailBlob(id: String): EmailBlob

    @Query("DELETE FROM emails_blob WHERE id = :id")
    suspend fun deleteEmailBlob(id: String)
}
