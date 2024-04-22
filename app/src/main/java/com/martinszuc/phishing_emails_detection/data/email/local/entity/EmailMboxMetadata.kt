package com.martinszuc.phishing_emails_detection.data.email.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents metadata for an email mbox file stored locally.
 * This entity class is used to persist mbox email file metadata in the local database,
 * tracking each file by a unique identifier and the timestamp of when it was processed or added.
 *
 * @param id The unique identifier for the mbox metadata entry.
 * @param timestamp The time at which the mbox file was processed or added, represented as a timestamp.
 *
 * Authored by matoszuc@gmail.com
 */
@Entity(tableName = "email_mbox_metadata")
data class EmailMboxMetadata(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
