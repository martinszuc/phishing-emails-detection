package com.martinszuc.phishing_emails_detection.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatTimestamp(timestamp: Long): String {
        // You can adjust the date format as needed
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

}