package com.martinszuc.phising_emails_detection.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.martinszuc.phising_emails_detection.data.EmailRepository

class EmailViewModelFactory(private val repository: EmailRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}