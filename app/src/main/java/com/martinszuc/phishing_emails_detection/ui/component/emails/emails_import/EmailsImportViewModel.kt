package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.local.repository.EmailFullLocalRepository
import com.martinszuc.phishing_emails_detection.data.local.repository.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.remote.repository.EmailFullRemoteRepository
import com.martinszuc.phishing_emails_detection.data.remote.repository.EmailMinimalRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailsImportViewModel @Inject constructor(
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
    private val emailMinimalRemoteRepository: EmailMinimalRemoteRepository,
    private val emailFullLocalRepository: EmailFullLocalRepository,
    private val emailFullRemoteRepository: EmailFullRemoteRepository
) : ViewModel() {

    private val _emailsFlow = MutableStateFlow<PagingData<EmailMinimal>>(PagingData.empty())
    val emailsFlow: Flow<PagingData<EmailMinimal>> = _emailsFlow.asStateFlow()
    val selectedEmails = MutableLiveData<List<EmailMinimal>>(emptyList())

    init {
        getEmails()
    }

    fun getEmails() {
        viewModelScope.launch {
            val pagingData = emailMinimalRemoteRepository.getEmails().first()
            _emailsFlow.value = pagingData
        }
    }

    fun searchEmails(query: String) {
        viewModelScope.launch {
            val pagingData = emailMinimalRemoteRepository.searchEmails(query).first()
            _emailsFlow.value = pagingData
        }
    }

    fun toggleEmailSelected(email: EmailMinimal) {
        val currentSelectedEmails = selectedEmails.value ?: emptyList()
        if (email in currentSelectedEmails) {
            selectedEmails.value = currentSelectedEmails - email
        } else {
            selectedEmails.value = currentSelectedEmails + email
        }
    }

    fun importSelectedEmails() {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedEmailsList = selectedEmails.value ?: return@launch

            // Fetch the full format of the selected emails
            val fullEmails = emailFullRemoteRepository.getEmailsFullByIds(selectedEmailsList.map { it.id })
            emailFullLocalRepository.insertAllEmailsFull(fullEmails)

            // Also save the minimal emails to the local database
            emailMinimalLocalRepository.insertAll(selectedEmailsList)

            selectedEmails.postValue(emptyList())
        }
    }
}
