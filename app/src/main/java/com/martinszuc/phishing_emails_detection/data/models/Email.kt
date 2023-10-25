package com.martinszuc.phishing_emails_detection.data.models

data class Email(
    val from: String,
    val subject: String,
    val body: String,
    var isSelected: Boolean = false

)