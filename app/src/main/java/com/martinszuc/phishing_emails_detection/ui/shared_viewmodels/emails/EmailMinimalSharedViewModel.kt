package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.remote.repository.EmailMinimalRemoteRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
@HiltViewModel
class EmailMinimalSharedViewModel @Inject constructor(
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
    private val emailMinimalRemoteRepository: EmailMinimalRemoteRepository,
) : AbstractBaseViewModel() {

    private val _localEmailsFlow = MutableStateFlow<PagingData<EmailMinimal>>(PagingData.empty())
    val localEmailsFlow: Flow<PagingData<EmailMinimal>> =
        _localEmailsFlow.asStateFlow().cachedIn(viewModelScope)

    private val _remoteEmailsFlow = MutableStateFlow<PagingData<EmailMinimal>>(PagingData.empty())
    val remoteEmailsFlow: Flow<PagingData<EmailMinimal>> =
        _remoteEmailsFlow.asStateFlow().cachedIn(viewModelScope)

    private val _emailsLoaded = MutableLiveData<Boolean>(false)
    val emailsLoaded: LiveData<Boolean> = _emailsLoaded

    private val _emailById = MutableLiveData<EmailMinimal?>()
    val emailById: LiveData<EmailMinimal?> = _emailById
    init {
        getLocalEmails()
    }

    fun getLocalEmails() {
        collectFlow(
            flow = emailMinimalLocalRepository.getAllEmailsForDetector().cachedIn(viewModelScope),
            onEach = { _localEmailsFlow.value = it }
        )
    }

    fun getRemoteEmails() {
        launchDataLoad(
            execution = {
                emailMinimalRemoteRepository.getEmails().cachedIn(viewModelScope).first()
            },
            onSuccess = {
                _remoteEmailsFlow.value = it
                _emailsLoaded.value = true
            }
        )
    }

    fun fetchEmailById(emailId: String) {
        launchDataLoad(
            execution = { emailMinimalLocalRepository.getEmailById(emailId) },
            onSuccess = { _emailById.postValue(it) }
        )
    }

    fun searchRemoteEmails(query: String) {
        launchDataLoad(
            execution = { emailMinimalRemoteRepository.searchEmails(query).first() },
            onSuccess = { _remoteEmailsFlow.value = it }
        )
    }

    fun clearIdFetchedEmail() {
        _emailById.postValue(null)
    }
}
