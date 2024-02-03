package com.martinszuc.phishing_emails_detection.ui.component.detector

/**
 * Authored by matoszuc@gmail.com
 */
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.local.repository.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.model.Classifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetectorViewModel @Inject constructor(
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
    private val emailBlobLocalRepository: EmailBlobLocalRepository,
    private val classifier: Classifier
) : ViewModel() {

    private val _selectedEmailId = MutableLiveData<String?>(null)
    val selectedEmailId: LiveData<String?> = _selectedEmailId

    private val _classificationResult = MutableLiveData<Boolean>()
    val classificationResult: LiveData<Boolean> = _classificationResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFinished = MutableLiveData(false)
    val isFinished: LiveData<Boolean> = _isFinished

    init {
        Log.d("DetectorViewModel", "Initializing ViewModel")
        viewModelScope.launch {
            val latestEmail = getLatestEmailId()
            _selectedEmailId.value = latestEmail
        }
        loadModel()
    }

    private suspend fun getLatestEmailId(): String? {
        return withContext(Dispatchers.IO) {
            emailMinimalLocalRepository.getLatestEmailId()
        }
    }

    suspend fun getMinimalEmailById(emailId: String): EmailMinimal? {
        return emailMinimalLocalRepository.getEmailById(emailId)
    }

    fun toggleEmailSelected(emailId: String) {
        // Set the new email ID, or deselect if it's the same ID
        _selectedEmailId.value = if (_selectedEmailId.value == emailId) null else emailId
    }

    fun clearSelectedEmail() {
        Log.d("DetectorViewModel", "Deselecting all emails")
        _selectedEmailId.value = null
    }


    fun classifySelectedMinimalEmail() {
        val emailId = _selectedEmailId.value
        Log.d("DetectorViewModel", "Processing email for detection: $emailId")
        if (emailId == null) {
            Log.d("DetectorViewModel", "No email selected for processing")
            return
        }
        viewModelScope.launch {
            Log.d("DetectorViewModel", "Fetching full email")
            _isLoading.value = true
            val fullEmail = emailMinimalLocalRepository.getEmailById(emailId)
            if (fullEmail == null) {
                Log.d("DetectorViewModel", "Full email is null")
                return@launch
            }

            Log.d("DetectorViewModel", "Classifying email")
            val result = classifier.classify(emailBlobLocalRepository.getMboxById(emailId))
            _classificationResult.value = result

            _isLoading.value = false
            _isFinished.value = true
            Log.d("DetectorViewModel", "Classification result: $result")
        }
    }

    fun loadModel() {
        viewModelScope.launch(Dispatchers.Main) {
            _isLoading.value = true

            // Switch to IO thread for loading model
            withContext(Dispatchers.IO) {
                classifier.loadModel()
            }

            // Switch back to Main thread to update LiveData
            _isLoading.value = false
        }
    }
}
