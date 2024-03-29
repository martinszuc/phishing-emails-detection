package com.martinszuc.phishing_emails_detection.data.email.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinszuc.phishing_emails_detection.data.email.local.entity.Subject

/**
 * Authored by matoszuc@gmail.com
 */

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: Subject)

    @Query("SELECT email_id FROM subject WHERE value LIKE '%' || :query || '%'")
    suspend fun searchIdsBySubject(query: String): List<String>

    @Query("DELETE FROM subject")
    suspend fun clearAll()
}
