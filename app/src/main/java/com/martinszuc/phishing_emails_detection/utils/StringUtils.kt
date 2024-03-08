package com.martinszuc.phishing_emails_detection.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Authored by matoszuc@gmail.com
 */

object StringUtils {
    fun formatTimestamp(timestamp: Long): String {
        // You can adjust the date format as needed
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    fun formatTimestampForFilename(timestamp: Long): String {
        // You can adjust the date format as needed
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun parseTimestampFromFilename(dateTimeStr: String): Date {
        val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return format.parse(dateTimeStr) ?: Date()
    }

}