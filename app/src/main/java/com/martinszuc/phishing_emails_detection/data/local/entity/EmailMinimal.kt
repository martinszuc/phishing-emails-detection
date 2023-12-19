package com.martinszuc.phishing_emails_detection.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "email_minimal")
data class EmailMinimal(
    @PrimaryKey(autoGenerate = true)
    val rowId: Int = 0,
    val id: String,
    val sender: String,
    val subject: String,
    val body: String,
    val timestamp: Long,
    val isPhishing: Boolean? = null)