package com.martinszuc.phishing_emails_detection.data.email_package.entity

data class EmailPackageMetadata(
    val fileName: String,
    val isPhishy: Boolean,
    val creationDate: Long // Unix timestamp
)