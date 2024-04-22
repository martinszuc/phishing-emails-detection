package com.martinszuc.phishing_emails_detection.ui.component.detector

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailMboxLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.file.FileRepository
import com.martinszuc.phishing_emails_detection.data.python.model.Prediction
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.ModelMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val logTag = "DetectorViewModel"

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class DetectorViewModel @Inject constructor(
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
    private val emailMboxLocalRepository: EmailMboxLocalRepository,
    private val fileRepository: FileRepository,
    private val prediction: Prediction,                                              // TODO notification when model isn't loaded
    @ApplicationContext private val context: Context  // Injecting application context

) : ViewModel() {

    private val _selectedEmailId = MutableLiveData<String?>(null)
    val selectedEmailId: LiveData<String?> = _selectedEmailId

    private val _classificationResult = MutableLiveData<Boolean>()
    val classificationResult: LiveData<Boolean> = _classificationResult

    private val _selectedModel = MutableLiveData<ModelMetadata?>()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFinished = MutableLiveData(false)
    val isFinished: LiveData<Boolean> = _isFinished

    init {
        Log.d(logTag, "Initializing ViewModel")
        viewModelScope.launch {
            val latestEmail = getLatestEmailId()
            _selectedEmailId.value = latestEmail
        }
    }

    fun toggleSelectedModel(modelMetadata: ModelMetadata) {
        val currentSelectedModel = _selectedModel.value
        if (currentSelectedModel == modelMetadata) {
            // If the same model is selected again, deselect it
//            _selectedModel.value = null
        } else {
            // Select the new model
            _selectedModel.value = modelMetadata
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
        Log.d(logTag, "Deselecting all emails")
        _selectedEmailId.value = null
    }

    fun clearSelectedModel() {
        Log.d(logTag, "Deselecting all emails")
        _selectedEmailId.value = null
    }

    fun clearIsFinished() {
        Log.d(logTag, "Deselecting all emails")
        _isFinished.value = false
    }


    fun classifySelectedMinimalEmail() {
        val emailId = _selectedEmailId.value
        val selectedModel = _selectedModel.value
        if (emailId == null || selectedModel == null) {
            Log.d(logTag, "No email or model selected for processing")
            _classificationResult.postValue(false)
            return
        }

        viewModelScope.launch {
            _isLoading.postValue(true)
            Log.d(logTag, "Fetching mbox content for email ID: $emailId")

            val mboxContent = emailMboxLocalRepository.fetchMboxContentById("$emailId.mbox")

            // Check if mboxContent is either null or empty
            if (mboxContent.isNullOrEmpty()) {
                Log.d(logTag, "Mbox content is empty or not available")
                _classificationResult.postValue(false)
                _isLoading.postValue(false)
                return@launch
            }

            // Save mbox content to a file
            val mboxFile = fileRepository.saveMboxForPrediction(context, mboxContent, "email_$emailId.mbox")
            Log.d(logTag, "Mbox content saved to file: ${mboxFile.name}")

            // Use the selected model name
            val modelName = selectedModel.modelName
            Log.d(logTag, "Preparing to classify email. Model name: $modelName, Mbox file path: ${mboxFile.absolutePath}")

            // Perform classification using the saved file and model path
            Log.d(logTag, "Classifying email from saved mbox file")
            val result = withContext(Dispatchers.IO) {
                prediction.classify(modelName, mboxFile.name)  // Assume this method correctly handles classification
            }

            // Check the first email prediction in the list to see if it's classified as phishing or not
            val isPhishing = result.firstOrNull()?.let { it > 0.45 } ?: false

            _classificationResult.postValue(isPhishing)

            _isLoading.postValue(false)
            _isFinished.postValue(true)
            Log.d(logTag, "Classification result for the first email: $isPhishing")
        }
    }

}