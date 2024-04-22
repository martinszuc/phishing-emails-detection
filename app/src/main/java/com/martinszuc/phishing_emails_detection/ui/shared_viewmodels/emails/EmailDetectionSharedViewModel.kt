package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.email_full.EmailDetection
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailDetectionLocalRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class EmailDetectionSharedViewModel @Inject constructor(
    private val emailDetectionLocalRepository: EmailDetectionLocalRepository
) : AbstractBaseViewModel() {

    private val _localEmailDetectionsFlow = MutableStateFlow<PagingData<EmailDetection>>(PagingData.empty())
    val localEmailDetectionsFlow: Flow<PagingData<EmailDetection>> = _localEmailDetectionsFlow.asStateFlow().cachedIn(viewModelScope)

    private val _emailDetectionById = MutableLiveData<EmailDetection?>()
    val emailDetectionById: LiveData<EmailDetection?> = _emailDetectionById

    init {
        getEmailDetections()
    }

    fun clearDatabase() {
        launchDataLoad(execution = {
            emailDetectionLocalRepository.clearAll()
        })
    }

    fun getEmailDetections() {
        collectFlow(
            flow = emailDetectionLocalRepository.getAllEmailDetectionsFlow().cachedIn(viewModelScope),
            onEach = { pagingData ->
                _localEmailDetectionsFlow.value = pagingData
            }
        )
    }

    fun fetchEmailDetectionById(emailDetectionId: String) {
        launchDataLoad(execution = {
            emailDetectionLocalRepository.getEmailDetectionById(emailDetectionId)
        }, onSuccess = { emailDetection ->
            _emailDetectionById.postValue(emailDetection)
        })
    }

    fun clearIdFetchedEmailDetection() {
        _emailDetectionById.postValue(null)
    }
}