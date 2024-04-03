package com.martinszuc.phishing_emails_detection.data.email.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMboxMetadata

@Dao
interface EmailMboxMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(emailMboxMetadata: EmailMboxMetadata)

    @Query("SELECT * FROM email_mbox_metadata WHERE id = :id")
    suspend fun getEmailMbox(id: String): EmailMboxMetadata

    @Query("DELETE FROM email_mbox_metadata WHERE id = :id")
    suspend fun deleteEmailMbox(id: String)

    @Query("DELETE FROM email_mbox_metadata")
    suspend fun clearAll()
}