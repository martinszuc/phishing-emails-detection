package com.martinszuc.phishing_emails_detection.utils

import android.util.Base64
import android.util.Log
import com.martinszuc.phishing_emails_detection.data.processed_packages.entity.ProcessedPackageMetadata
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val logTag = "StringUtils"

/**
 * Authored by matoszuc@gmail.com
 */

object StringUtils {
    fun formatTimestamp(timestamp: Long): String {
        // You can adjust the date format as needed
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatBytes(bytes: Long): String {
        val kiloBytes = bytes / 1024.0
        val megaBytes = kiloBytes / 1024.0

        return when {
            megaBytes >= 1 -> String.format("%.1f MB", megaBytes)
            kiloBytes >= 1 -> String.format("%.1f KB", kiloBytes)
            else -> "$bytes B"
        }
    }

    fun formatTimestampForFilename(timestamp: Long): String {
        // You can adjust the date format as needed
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun parseTimestampFromFilename(dateTimeStr: String): Date {
        val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return format.parse(dateTimeStr) ?: Date()
    }

    fun parseCsvFilename(file: File): ProcessedPackageMetadata? {
        val filename = file.name
        // Updated Regex pattern to match the new filename format
        val pattern = Regex("(.+?)_(\\d{8}_\\d{6})_(\\d+?)_(phishy|safe)-export\\.csv$")
        val matchResult = pattern.matchEntire(filename)

        return matchResult?.let {
            val (packageName, dateTime, numberOfItems, isPhishyStr) = it.destructured
            val isPhishy = isPhishyStr == "phishy"
            val creationDate = parseTimestampFromFilename(dateTime).time
            val fileSize = file.length() // Using the File object directly for length
            val itemCount = numberOfItems.toInt()

            ProcessedPackageMetadata(
                fileName = filename,
                isPhishy = isPhishy,
                packageName = packageName,
                creationDate = creationDate,
                fileSize = fileSize,
                numberOfEmails = itemCount
            )
        }
    }

    fun generateClientId(): String = UUID.randomUUID().toString()

    fun countCsvRowsIgnoringEmptyLines(csvContent: String?): Int {
        if (csvContent.isNullOrEmpty()) {
            return 0 // Return 0 if the CSV content is null or empty
        }

        // Split the content by new line characters to get an array of rows, then filter out any empty lines
        val rows = csvContent.split(Regex("\r\n|\n|\r")).filter { it.isNotEmpty() }

        return rows.size // Return the number of non-empty rows
    }

    // Base64 URL-safe decoding
    fun decodeBase64UrlSafe(data: String?): String? {
        return try {
            val base64Decoded = Base64.decode(data, Base64.URL_SAFE)
            String(base64Decoded, Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            Log.e(logTag, "Error decoding Base64: ${e.message}")
            null
        }
    }
}