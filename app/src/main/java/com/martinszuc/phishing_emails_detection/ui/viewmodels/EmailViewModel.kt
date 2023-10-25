package com.martinszuc.phishing_emails_detection.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.martinszuc.phishing_emails_detection.data.EmailRepository
import com.martinszuc.phishing_emails_detection.data.models.Email
import kotlinx.coroutines.Dispatchers

class EmailViewModel(private val repository: EmailRepository) : ViewModel() {

    val emails = repository.fetchEmails().cachedIn(viewModelScope)

    fun toggleEmailSelected(email: Email) {
        email.isSelected = !email.isSelected
    }
}
