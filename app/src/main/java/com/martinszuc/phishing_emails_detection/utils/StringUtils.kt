package com.martinszuc.phishing_emails_detection.utils

import com.martinszuc.phishing_emails_detection.data.email_package.entity.ProcessedPackageMetadata
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
    fun parseCsvFilename(filename: String): ProcessedPackageMetadata? {
        // Updated Regex pattern to match the new filename format
        val pattern = Regex("(.+?)_(\\d{8}_\\d{6})_(\\d+?)_(phishy|safe)-export\\.csv$")
        val matchResult = pattern.matchEntire(filename)

        return matchResult?.let {
            val (packageName, dateTime, numberOfItems, isPhishyStr) = it.destructured
            val isPhishy = isPhishyStr == "phishy"
            val creationDate = StringUtils.parseTimestampFromFilename(dateTime).time
            val fileSize = File(Constants.OUTPUT_CSV_DIR, filename).length()
            val itemCount = numberOfItems.toInt() // Renamed to itemCount for clarity, can represent emails or features

            // Adjust the class or use the correct one according to your application structure
            ProcessedPackageMetadata(
                fileName = filename,
                isPhishy = isPhishy,
                packageName = packageName,
                creationDate = creationDate,
                fileSize = fileSize,
                numberOfEmails = itemCount // Assuming number of emails correlates with number of features
            )
        }
    }

    fun parseModelNameFromFilename(filename: String): String {
        val regex = Regex("/([^/]+)/[^/]+_phishy-export\\.csv$")
        return regex.find(filename)?.groups?.get(1)?.value ?: "defaultModelName"
    }
}