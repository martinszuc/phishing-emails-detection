package com.martinszuc.phishing_emails_detection.data.email.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Authored by matoszuc@gmail.com
 */
@Entity(tableName = "email_blob")
data class EmailBlob(
    @PrimaryKey
    val id: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val blob: ByteArray,
    @ColumnInfo(name = "sender_email")
    val senderEmail: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmailBlob

        if (id != other.id) return false
        if (!blob.contentEquals(other.blob)) return false
        if (senderEmail != other.senderEmail) return false
        return timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + blob.contentHashCode()
        result = 31 * result + senderEmail.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}