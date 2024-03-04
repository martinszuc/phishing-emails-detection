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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
@HiltViewModel
class EmailMinimalSharedViewModel @Inject constructor(
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()

    private val _emailsFlow = MutableStateFlow<PagingData<EmailMinimal>>(PagingData.empty())
    val emailsFlow: Flow<PagingData<EmailMinimal>> = _emailsFlow.asStateFlow()

    init {
        getEmails()
    }

    private fun getEmails() {
        viewModelScope.launch {
            Log.d("DetectorViewModel", "Fetching emails")
            val flow = emailMinimalLocalRepository.getAllEmailsForDetector().cachedIn(viewModelScope)
            _emailsFlow.emitAll(flow)
            Log.d("DetectorViewModel", "Emails fetched")
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

}