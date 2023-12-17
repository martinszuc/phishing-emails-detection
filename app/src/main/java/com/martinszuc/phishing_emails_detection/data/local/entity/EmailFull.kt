package com.martinszuc.phishing_emails_detection.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.Payload
import com.martinszuc.phishing_emails_detection.utils.Converters

/**
 * Authored by matoszuc@gmail.com
 */
@Entity(tableName = "email_full")
data class EmailFull(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "thread_id")
    val threadId: String,
    @ColumnInfo(name = "label_ids")
    @TypeConverters(Converters::class)
    val labelIds: List<String>,
    val snippet: String,
    @ColumnInfo(name = "history_id")
    val historyId: Long,
    @ColumnInfo(name = "internal_date")
    val internalDate: Long,
    @Embedded
    val payload: Payload
)







