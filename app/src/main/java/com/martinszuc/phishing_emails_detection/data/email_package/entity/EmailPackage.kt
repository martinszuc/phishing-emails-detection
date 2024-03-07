package com.martinszuc.phishing_emails_detection.data.email_package.entity

data class EmailPackage(
    val ids: List<String>,
    val isPhishy: Boolean,
    val mboxContent: String
)