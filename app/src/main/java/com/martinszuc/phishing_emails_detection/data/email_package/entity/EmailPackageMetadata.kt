package com.martinszuc.phishing_emails_detection.data.email_package.entity

data class EmailPackageMetadata(
    val fileName: String,
    val isPhishy: Boolean,
    val packageName: String,
    val creationDate: Long, // Unix timestamp
    val fileSize: Long, // File size in bytes
    val numberOfEmails: Int // Number of emails in the mbox
)