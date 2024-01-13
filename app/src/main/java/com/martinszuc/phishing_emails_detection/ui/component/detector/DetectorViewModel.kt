package com.martinszuc.phishing_emails_detection.ui.component.detector

/**
 * Authored by matoszuc@gmail.com
 */
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.local.repository.EmailFullLocalRepository
import com.martinszuc.phishing_emails_detection.data.local.repository.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.tensor.Classifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetectorViewModel @Inject constructor(
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
    private val emailFullLocalRepository: EmailFullLocalRepository,
    private val classifier: Classifier
) : ViewModel() {

    private val _selectedEmailId = MutableLiveData<String?>(null)
    val selectedEmailId: LiveData<String?> = _selectedEmailId

    private val _classificationResult = MutableLiveData<Float>()
    val classificationResult: LiveData<Float> = _classificationResult

    private val _emailsFlow = MutableStateFlow<PagingData<EmailMinimal>>(PagingData.empty())
    val emailsFlow: Flow<PagingData<EmailMinimal>> = _emailsFlow.asStateFlow()

    init {
        Log.d("DetectorViewModel", "Initializing ViewModel")
        getEmails()
    }

    fun getEmails() {
        viewModelScope.launch {
            Log.d("DetectorViewModel", "Fetching emails")
            val flow = emailMinimalLocalRepository.getAllEmails().cachedIn(viewModelScope)
            _emailsFlow.emitAll(flow)
            Log.d("DetectorViewModel", "Emails fetched")
        }
    }


    fun toggleEmailSelected(email: EmailMinimal) {
        Log.d("DetectorViewModel", "Toggling email selection: ${email.id}")
        _selectedEmailId.value = if (_selectedEmailId.value == email.id) null else email.id
    }

    fun processEmailForDetection() {
        val emailId = _selectedEmailId.value
        Log.d("DetectorViewModel", "Processing email for detection: $emailId")
        if (emailId == null) {
            Log.d("DetectorViewModel", "No email selected for processing")
            return
        }
        viewModelScope.launch {
            Log.d("DetectorViewModel", "Fetching full email")
            val fullEmail = emailFullLocalRepository.getEmailById(emailId)
            if (fullEmail == null) {
                Log.d("DetectorViewModel", "Full email is null")
                return@launch
            }
            Log.d("DetectorViewModel", "Classifying email")
            val result = classifier.classify(fullEmail.payload.body.data)
            _classificationResult.value = result
            Log.d("DetectorViewModel", "Classification result: $result")
        }
    }
}