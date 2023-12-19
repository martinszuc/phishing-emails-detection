package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.local.repository.EmailFullLocalRepository
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
class EmailsSavedViewModel @Inject constructor(
    private val emailFullLocalRepository: EmailFullLocalRepository
) : ViewModel() {

    private val _emailsFlow = MutableStateFlow<PagingData<EmailFull>>(PagingData.empty())
    val emailsFlow: Flow<PagingData<EmailFull>> = _emailsFlow.asStateFlow()

    init {
        getEmails()
    }

    fun getEmails() {
        viewModelScope.launch {
            emailFullLocalRepository.getAllEmailsFull().cachedIn(viewModelScope).collectLatest { pagingData ->
                _emailsFlow.value = pagingData
            }
        }
    }

    fun searchEmails(query: String) {
        viewModelScope.launch {
            emailFullLocalRepository.searchEmails(query).cachedIn(viewModelScope).collectLatest { pagingData ->
                _emailsFlow.value = pagingData
            }
        }
    }
    fun clearDatabase() {
        viewModelScope.launch {
            emailFullLocalRepository.clearAll()
        }
    }
}

