package com.martinszuc.phishing_emails_detection.utils

class Constants {
    companion object {
        const val GMAIL_PAGER_PREFETCH = 7
        const val GMAIL_PAGER_INIT = 10
        const val GMAIL_PAGER_PAGE = 11

        const val DATABASE_NAME = "phishing-emails-detection-database"
        const val GMAIL_READONLY_SCOPE = "https://www.googleapis.com/auth/gmail.readonly"
        const val APPLICATION_NAME = "Phishing emails detection"

    }
}