package com.martinszuc.phising_emails_detection.network

import com.martinszuc.phising_emails_detection.data.models.Email

interface IEmailApiService {
    suspend fun getEmails(): List<Email> // Define how to fetch emails
}