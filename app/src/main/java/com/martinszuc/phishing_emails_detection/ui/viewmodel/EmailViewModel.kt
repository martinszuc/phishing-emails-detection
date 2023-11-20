package com.martinszuc.phishing_emails_detection.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.martinszuc.phishing_emails_detection.data.entity.Email
import com.martinszuc.phishing_emails_detection.data.repository.EmailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailViewModel @Inject constructor(
    private val repository: EmailRepository
) : ViewModel() {

    private val _emailsFlow = MutableStateFlow<PagingData<Email>>(PagingData.empty())
    val emailsFlow: Flow<PagingData<Email>> = _emailsFlow.asStateFlow()


    init {
        getEmails()
    }

    fun getEmails() {
        viewModelScope.launch {
            val pagingData = repository.getEmails().first()
            _emailsFlow.value = pagingData
        }
    }

    fun searchEmails(query: String) {
        viewModelScope.launch {
            val pagingData = repository.searchEmails(query).first()
            _emailsFlow.value = pagingData
        }
    }
    fun saveEmails(emails: List<Email>) {
        viewModelScope.launch {
            repository.insertAll(emails)
        }
    }

    fun toggleEmailSelected(email: Email) {
        email.isSelected = !email.isSelected
    }
}
