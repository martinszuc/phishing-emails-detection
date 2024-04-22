package com.martinszuc.phishing_emails_detection.data.email_package.entity

/**
 * Represents the metadata for an email package in the application. This class holds details about the package,
 * such as its file name, phishing status, package name, creation date, file size, and the count of emails it contains.
 * This metadata facilitates the management and display of email packages stored by the application.
 *
 * Authored by matoszuc@gmail.com
 */
data class EmailPackageMetadata(
    val fileName: String,
    val isPhishy: Boolean,
    val packageName: String,
    val creationDate: Long, // Unix timestamp
    val fileSize: Long, // File size in bytes
    val numberOfEmails: Int // Number of emails in the mbox
)
