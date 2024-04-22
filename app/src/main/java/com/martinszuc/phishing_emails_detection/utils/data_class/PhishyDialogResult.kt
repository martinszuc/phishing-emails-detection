package com.martinszuc.phishing_emails_detection.utils.data_class

data class PhishyDialogResult(
    val isPhishy: Boolean,
    val packageName: String?,
    val wasCancelled: Boolean = false
)