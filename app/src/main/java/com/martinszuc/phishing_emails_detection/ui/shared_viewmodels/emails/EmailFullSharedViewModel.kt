package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailFullLocalRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailFullSharedViewModel @Inject constructor(
    private val emailFullLocalRepository: EmailFullLocalRepository
) : AbstractBaseViewModel() {

    private val _localEmailsFlow = MutableStateFlow<PagingData<EmailFull>>(PagingData.empty())
    val localEmailsFlow: Flow<PagingData<EmailFull>> = _localEmailsFlow.asStateFlow().cachedIn(viewModelScope)

    private val _emailById = MutableLiveData<EmailFull?>()
    val emailById: LiveData<EmailFull?> = _emailById

    init {
        getEmails()
    }

    fun searchEmails(query: String) {
        viewModelScope.launch {
            collectFlow(
                flow = emailFullLocalRepository.searchEmails(query).cachedIn(viewModelScope),
                onEach = { pagingData ->
                    _localEmailsFlow.value = pagingData
                }
            )
        }
    }


    fun getEmails() {
        collectFlow(
            flow = emailFullLocalRepository.getAllEmailsFull().cachedIn(viewModelScope),
            onEach = { pagingData ->
                _localEmailsFlow.value = pagingData
            }
        )
    }

    fun fetchEmailById(emailId: String) {
        launchDataLoad(execution = {
            emailFullLocalRepository.getEmailById(emailId)
        }, onSuccess = { email ->
            _emailById.postValue(email)
        })
    }

    fun clearIdFetchedEmail() {
        _emailById.postValue(null)
    }
}
