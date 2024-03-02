package com.martinszuc.phishing_emails_detection.ui.component.detector

/**
 * Authored by matoszuc@gmail.com
 */
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.model.Model
import com.martinszuc.phishing_emails_detection.utils.emails.EmailUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetectorViewModel @Inject constructor(
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
    private val emailBlobLocalRepository: EmailBlobLocalRepository,
    private val model: Model,                                              // TODO notificaiton when model isnt loaded
    @ApplicationContext private val context: Context  // Injecting application context

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
        if (emailId == null) {
            Log.d("DetectorViewModel", "No email selected for processing")
            _classificationResult.postValue(false)
            return
        }

        viewModelScope.launch {
            _isLoading.postValue(true)
            Log.d("DetectorViewModel", "Fetching mbox content for email ID: $emailId")

            val mboxContent = emailBlobLocalRepository.getMboxById(emailId)
            if (mboxContent.isEmpty()) {
                Log.d("DetectorViewModel", "Mbox content is empty")
                _classificationResult.postValue(false)
                _isLoading.postValue(false)
                return@launch
            }

            // Save mbox content to a file
            val mboxFile = EmailUtils.saveMboxToFile(context, mboxContent, "email_$emailId.mbox")
            Log.d("DetectorViewModel", "Mbox content saved to file: ${mboxFile.absolutePath}")

            // Perform classification using the saved file
            Log.d("DetectorViewModel", "Classifying email from saved mbox file")
            val result = model.classify(mboxFile.absolutePath)  // Assume model.classify now accepts a file path
            _classificationResult.postValue(result)

            _isLoading.postValue(false)
            _isFinished.postValue(true)
            Log.d("DetectorViewModel", "Classification result: $result")
        }
    }
}