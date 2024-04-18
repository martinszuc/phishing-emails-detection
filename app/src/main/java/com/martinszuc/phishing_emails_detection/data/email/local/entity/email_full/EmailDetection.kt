package com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "email_detection")
data class EmailDetection(
    @PrimaryKey
    val id_detection: String,
    @Embedded
    val emailFull: EmailFull,
    @ColumnInfo(name = "is_phishing")
    val isPhishing: Boolean
) : Parcelable