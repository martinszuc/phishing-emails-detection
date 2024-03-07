package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detector

sealed class EmailPackageItem {
    data class EmailPackage(val id: Int, val packageName: String, val isPhishy: Boolean, val creationDate: Long) : EmailPackageItem()
    object AddNewItem : EmailPackageItem()
}