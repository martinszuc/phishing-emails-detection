package com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
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
    val payload: Payload,
    @ColumnInfo(name = "is_phishing")
    val isPhishing: Boolean?
) : Parcelable



//@kotlinx.parcelize.Parcelize
//data class Body(
//    val data: String,
//    val size: Int
//) : Parcelable
//@kotlinx.android.parcel.Parcelize
//@Entity(tableName = "email_full")
//data class EmailFull(
//    @PrimaryKey
//    val id: String,
//    @ColumnInfo(name = "thread_id")
//    val threadId: String,
//    @ColumnInfo(name = "label_ids")
//    @TypeConverters(Converters::class)
//    val labelIds: List<String>,
//    val snippet: String,
//    @ColumnInfo(name = "history_id")
//    val historyId: Long,
//    @ColumnInfo(name = "internal_date")
//    val internalDate: Long,
//    @Embedded
//    val payload: Payload
//) : Parcelable
//@kotlinx.parcelize.Parcelize
//data class Header(
//    val name: String,
//    val value: String
//) : Parcelable@kotlinx.android.parcel.Parcelize
//data class Part(
//    @ColumnInfo(name = "part_id")
//    val partId: String?,
//    @ColumnInfo(name = "mime_type")
//    val mimeType: String,
//    val filename: String?,
//    @TypeConverters(Converters::class)
//    val headers: List<Header>,
//    @Embedded
//    val body: Body
//) : Parcelable@kotlinx.android.parcel.Parcelize
//data class Payload(
//    @ColumnInfo(name = "part_id")
//    val partId: String?,
//    @ColumnInfo(name = "mime_type")
//    val mimeType: String,
//    val filename: String?,
//    @TypeConverters(Converters::class)
//    val headers: List<Header>,
//    @Embedded
//    val body: Body?,
//    @TypeConverters(Converters::class)
//    val parts: List<Part>?
//) : Parcelable




