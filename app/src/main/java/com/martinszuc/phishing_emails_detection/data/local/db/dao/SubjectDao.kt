package com.martinszuc.phishing_emails_detection.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinszuc.phishing_emails_detection.data.local.entity.Subject

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: Subject)

    @Query("SELECT email_id FROM subject WHERE value LIKE '%' || :query || '%'")
    suspend fun searchIdsBySubject(query: String): List<String>

}
