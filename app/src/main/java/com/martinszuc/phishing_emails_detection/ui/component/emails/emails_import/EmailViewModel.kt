package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.martinszuc.phishing_emails_detection.data.local.entity.Email
import com.martinszuc.phishing_emails_detection.data.local.repository.EmailLocalRepository
import com.martinszuc.phishing_emails_detection.data.remote.repository.EmailRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailViewModel @Inject constructor(
    private val localRepository: EmailLocalRepository,
    private val remoteRepository: EmailRemoteRepository
) : ViewModel() {

    private val _emailsFlow = MutableStateFlow<PagingData<Email>>(PagingData.empty())
    val emailsFlow: Flow<PagingData<Email>> = _emailsFlow.asStateFlow()
    val selectedEmails = MutableLiveData<List<Email>>(emptyList())



    init {
        getEmails()
    }

    fun getEmails() {
        viewModelScope.launch {
            val pagingData = remoteRepository.getEmails().first()
            _emailsFlow.value = pagingData
        }
    }

    fun searchEmails(query: String) {
        viewModelScope.launch {
            val pagingData = remoteRepository.searchEmails(query).first()
            _emailsFlow.value = pagingData
        }
    }

    fun toggleEmailSelected(email: Email) {
        val currentSelectedEmails = selectedEmails.value ?: emptyList()
        if (email in currentSelectedEmails) {
            selectedEmails.value = currentSelectedEmails - email
        } else {
            selectedEmails.value = currentSelectedEmails + email
        }
    }

    fun saveSelectedEmailsToDatabase() {
        val emailsToSave = selectedEmails.value ?: emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            emailsToSave.forEach { email ->
                localRepository.insert(email)
            }
        }
        selectedEmails.value = emptyList()
    }

}
