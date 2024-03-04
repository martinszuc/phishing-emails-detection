package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailFullLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
@HiltViewModel
class EmailFullSharedViewModel  @Inject constructor(
    private val emailFullLocalRepository: EmailFullLocalRepository
) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()

    private val _localEmailsFlow = MutableStateFlow<PagingData<EmailFull>>(PagingData.empty())
    val localEmailsFlow: Flow<PagingData<EmailFull>> = _localEmailsFlow.asStateFlow()

    init {
        getEmails()
    }

    fun searchEmails(query: String) {
        viewModelScope.launch {
            emailFullLocalRepository.searchEmails(query).cachedIn(viewModelScope).collectLatest { pagingData ->
                _localEmailsFlow.value = pagingData
            }
        }
    }

    fun getEmails() {
        viewModelScope.launch {
            emailFullLocalRepository.getAllEmailsFull().cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    _localEmailsFlow.value = pagingData
                }
        }
    }

    private val _emailById = MutableLiveData<EmailFull?>()
    val emailById: LiveData<EmailFull?> = _emailById

    fun fetchEmailById(emailId: String) {
        isLoading.value = true
        viewModelScope.launch {
            val email = emailFullLocalRepository.getEmailById(emailId)
            _emailById.postValue(email)
            isLoading.value = false
        }
    }

    fun clearIdFetchedEmail() {
        _emailById.postValue(null)
    }


}
