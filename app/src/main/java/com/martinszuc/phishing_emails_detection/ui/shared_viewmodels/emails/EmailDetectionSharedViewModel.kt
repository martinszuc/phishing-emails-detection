package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailDetection
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailDetectionLocalRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

//    fun searchEmailDetections(query: String) {
//        viewModelScope.launch {
//            collectFlow(
//                flow = emailDetectionLocalRepository.searchEmailDetections(query).cachedIn(viewModelScope),
//                onEach = { pagingData ->
//                    _localEmailDetectionsFlow.value = pagingData
//                }
//            )
//        }
//    }

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