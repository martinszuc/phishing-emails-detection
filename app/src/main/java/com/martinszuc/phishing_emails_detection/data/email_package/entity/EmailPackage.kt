package com.martinszuc.phishing_emails_detection.data.email_package.entity

data class EmailPackage(
    val id: Int, // Assuming there's an ID
    val isPhishy: Boolean,
    val packageName: String,
    val creationDate: Long // Use a Long to store dates as timestamps
)