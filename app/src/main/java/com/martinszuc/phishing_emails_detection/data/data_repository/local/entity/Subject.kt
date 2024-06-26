package com.martinszuc.phishing_emails_detection.data.data_repository.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author matoszuc@gmail.com
 */
@Entity(tableName = "subject")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "email_id")
    val emailId: String,
    val value: String
)