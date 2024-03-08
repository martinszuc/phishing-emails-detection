package com.martinszuc.phishing_emails_detection.utils

import com.martinszuc.phishing_emails_detection.data.email_package.entity.ProcessedPackageMetadata
import java.io.File

object EPackageUtils {
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

}