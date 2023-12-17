package com.martinszuc.phishing_emails_detection.data.local.entity.email_full

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.TypeConverters
import com.martinszuc.phishing_emails_detection.utils.Converters

/**
 * Authored by matoszuc@gmail.com
 */
data class Part(
    @ColumnInfo(name = "part_id")
    val partId: String,
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    val filename: String,
    @TypeConverters(Converters::class)
    val headers: List<Header>,
    @Embedded
    val body: Body
)