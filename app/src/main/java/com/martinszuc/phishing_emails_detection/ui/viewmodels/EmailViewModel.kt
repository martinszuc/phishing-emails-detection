package com.martinszuc.phishing_emails_detection.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.martinszuc.phishing_emails_detection.data.EmailRepository
import com.martinszuc.phishing_emails_detection.data.models.Email
import kotlinx.coroutines.Dispatchers

class EmailViewModel(private val repository: EmailRepository) : ViewModel() {

    val emails = liveData(Dispatchers.IO) {
        emit(repository.fetchEmails()) // Fetch emails from repository
    }

    fun toggleEmailSelected(email: Email) {
        email.isSelected = !email.isSelected
    }
}
