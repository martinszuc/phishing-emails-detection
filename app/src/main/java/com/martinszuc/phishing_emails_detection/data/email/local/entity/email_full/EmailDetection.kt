package com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents an email detection entry with phishing labeling.
 * This entity includes detailed email data and a phishing classification.
 * It's used to store and retrieve email detection data from the local database.
 *
 * @author matoszuc@gmail.com
 */
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
