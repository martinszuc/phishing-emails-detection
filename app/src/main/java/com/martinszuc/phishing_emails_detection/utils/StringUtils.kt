package com.martinszuc.phishing_emails_detection.utils

import com.martinszuc.phishing_emails_detection.data.processed_packages.entity.ProcessedPackageMetadata
import java.io.File
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

    fun parseTimestampFromFilename(dateTimeStr: String): Date {
        val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return format.parse(dateTimeStr) ?: Date()
    }

    fun generateModelDirectoryName(modelName: String): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        return "${modelName}_${dateStr}"
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


    fun parseModelNameFromFilename(filename: String): String {
        val regex = Regex("/([^/]+)/[^/]+_phishy-export\\.csv$")
        return regex.find(filename)?.groups?.get(1)?.value ?: "defaultModelName"
    }

    fun countCsvRowsIgnoringEmptyLines(csvContent: String?): Int {
        if (csvContent == null || csvContent.isEmpty()) {
            return 0 // Return 0 if the CSV content is null or empty
        }

        // Split the content by new line characters to get an array of rows, then filter out any empty lines
        val rows = csvContent.split(Regex("\r\n|\n|\r")).filter { it.isNotEmpty() }

        return rows.size // Return the number of non-empty rows
    }
}