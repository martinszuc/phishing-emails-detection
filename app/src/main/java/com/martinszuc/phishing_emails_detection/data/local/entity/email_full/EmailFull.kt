package com.martinszuc.phishing_emails_detection.data.local.entity.email_full

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.Payload
import com.martinszuc.phishing_emails_detection.utils.Converters
import kotlinx.android.parcel.Parcelize

/**
 * Authored by matoszuc@gmail.com
 */
@Parcelize
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
) : Parcelable







