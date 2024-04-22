package com.martinszuc.phishing_emails_detection.data.email.local.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailDetection

/**
 * Authored by matoszuc@gmail.com
 */

@Dao
interface EmailDetectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(emailDetection: EmailDetection)

    @Query("SELECT * FROM email_detection WHERE id = :id")
    suspend fun getEmailDetectionById(id: String): EmailDetection?

    @Query("SELECT * FROM email_detection")
    suspend fun getAllEmailDetections(): List<EmailDetection>

    @Query("SELECT * FROM email_detection")
    fun getAllEmailDetectionsPaged(): PagingSource<Int, EmailDetection>

    @Delete
    suspend fun delete(emailDetection: EmailDetection)

    @Update
    suspend fun update(emailDetection: EmailDetection)

    @Query("DELETE FROM email_detection")
    suspend fun clearAll()

    @Query("SELECT EXISTS(SELECT 1 FROM email_detection WHERE id_detection = :id)")
    suspend fun isEmailSaved(id: String): Boolean
}