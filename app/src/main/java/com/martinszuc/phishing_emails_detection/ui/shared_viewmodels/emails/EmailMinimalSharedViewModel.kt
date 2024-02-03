package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.local.repository.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.remote.repository.EmailMinimalRemoteRepository
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
) : ViewModel() {
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

}