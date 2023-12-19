package com.martinszuc.phishing_emails_detection.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Authored by matoszuc@gmail.com
 */
@Entity(tableName = "emails_blob")
data class EmailBlob(
    @PrimaryKey
    val id: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val blob: ByteArray
)