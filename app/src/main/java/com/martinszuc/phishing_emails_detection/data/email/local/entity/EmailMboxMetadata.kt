package com.martinszuc.phishing_emails_detection.data.email.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "email_mbox_metadata")
data class EmailMboxMetadata(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)