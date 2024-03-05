package com.martinszuc.phishing_emails_detection.utils

/**
 * Authored by matoszuc@gmail.com
 */

class Constants {
    companion object {
        const val GMAIL_PAGER_PREFETCH = 7
        const val GMAIL_PAGER_INIT = 10
        const val GMAIL_PAGER_PAGE = 7

        const val DB_PAGER_SIZE = 20

        const val DATABASE_NAME = "phishing-emails-detection-database"
        const val GMAIL_READONLY_SCOPE = "https://www.googleapis.com/auth/gmail.readonly"
        const val APPLICATION_NAME = "Phishing emails detection"

        const val PHISHING_QUIZ_LINK = "https://phishingquiz.withgoogle.com/"
        const val PHISHING_INFO_LINK = "https://consumer.ftc.gov/articles/how-recognize-and-avoid-phishing-scams"

        // GmailApiService constants

        const val SENDER_UNKNOWN = "unknown@sender.com"

    }
}