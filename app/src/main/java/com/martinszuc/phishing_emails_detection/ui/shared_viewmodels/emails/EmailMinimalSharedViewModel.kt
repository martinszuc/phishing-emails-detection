package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.remote.repository.EmailMinimalRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
@HiltViewModel
class EmailMinimalSharedViewModel @Inject constructor(
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
    private val emailMinimalRemoteRepository: EmailMinimalRemoteRepository,
) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()

    private val _localEmailsFlow = MutableStateFlow<PagingData<EmailMinimal>>(PagingData.empty())
    val localEmailsFlow: Flow<PagingData<EmailMinimal>> = _localEmailsFlow.asStateFlow().cachedIn(viewModelScope)

    private val _remoteEmailsFlow = MutableStateFlow<PagingData<EmailMinimal>>(PagingData.empty())
    val remoteEmailsFlow: Flow<PagingData<EmailMinimal>> = _remoteEmailsFlow.asStateFlow().cachedIn(viewModelScope)

    private val _emailsLoaded = MutableLiveData<Boolean>(false)
    val emailsLoaded: LiveData<Boolean> = _emailsLoaded

    init {
        getLocalEmails()
    }

    fun getLocalEmails() {
        viewModelScope.launch {
            Log.d("DetectorViewModel", "Fetching emails")
            val flow = emailMinimalLocalRepository.getAllEmailsForDetector().cachedIn(viewModelScope)
            _localEmailsFlow.emitAll(flow)
            Log.d("DetectorViewModel", "Emails fetched")
        }
    }

    fun getRemoteEmails() {
        viewModelScope.launch {
            val pagingData = emailMinimalRemoteRepository.getEmails().cachedIn(viewModelScope).first()
            _remoteEmailsFlow.value = pagingData
            _emailsLoaded.value = true
        }
    }


    private val _emailById = MutableLiveData<EmailMinimal?>()
    val emailById: LiveData<EmailMinimal?> = _emailById

    fun fetchEmailById(emailId: String) {
        isLoading.value = true
        viewModelScope.launch {
            val email = emailMinimalLocalRepository.getEmailById(emailId)
            _emailById.postValue(email)
            isLoading.value = false
        }
    }

    fun clearIdFetchedEmail() {
        _emailById.postValue(null)
    }

    fun searchRemoteEmails(query: String) {
        viewModelScope.launch {
            val pagingData = emailMinimalRemoteRepository.searchEmails(query).first()
            _remoteEmailsFlow.value = pagingData
        }
    }

    fun checkIfEmailsLoaded() = emailsLoaded.value ?: false

}