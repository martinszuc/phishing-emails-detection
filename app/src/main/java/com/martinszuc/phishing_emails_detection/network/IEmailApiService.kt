package com.martinszuc.phishing_emails_detection.network

import com.martinszuc.phishing_emails_detection.data.models.Email

interface IEmailApiService {
    suspend fun getEmails(page: Int, pageSize: Int): List<Email> // Define how to fetch emails
}