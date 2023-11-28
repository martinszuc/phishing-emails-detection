package com.martinszuc.phishing_emails_detection.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emails")
data class Email(
    @PrimaryKey(autoGenerate = true)
    val rowId: Int = 0,
    val id: String,
    val sender: String,
    val subject: String,
    val body: String,
    val timestamp: Long,
    val isPhishing: Boolean? = null)