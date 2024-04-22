package com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.TypeConverters
import com.martinszuc.phishing_emails_detection.utils.Converters
import kotlinx.android.parcel.Parcelize

/**
 * @author matoszuc@gmail.com
 */

@Parcelize
data class Payload(
    @ColumnInfo(name = "part_id")
    val partId: String?,
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    val filename: String?,
    @TypeConverters(Converters::class)
    val headers: List<Header>,
    @Embedded
    val body: Body,
    @TypeConverters(Converters::class)
    val parts: List<Part>?
) : Parcelable