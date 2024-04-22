package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detection

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailDetectionLocalRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val logTag = "EmailsDetectionViewModel"

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class EmailsDetectionViewModel @Inject constructor(
    private val emailDetectionLocalRepository: EmailDetectionLocalRepository,
) : AbstractBaseViewModel() {

    private val _isSelectionMode = MutableLiveData(false)
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    private val _selectedEmails = MutableLiveData<Set<String>>(setOf())
    val selectedEmails: LiveData<Set<String>> = _selectedEmails

    fun toggleEmailSelected(id: String) {
        val currentSelected = _selectedEmails.value ?: emptySet()
        _selectedEmails.value = currentSelected.toMutableSet().apply {
            if (contains(id)) remove(id) else add(id)
        }
    }

    suspend fun saveEmlToEmailDetection(uri: Uri, isPhishy: Boolean) {
        launchDataLoad(
            execution = {
                emailDetectionLocalRepository.processEmlFileToEmailDetection(uri, isPhishy)
            },
            onSuccess = {
                // Handle success case, post success event or message if necessary
                Log.i(logTag, "EML file processed successfully.")
            },
            onFailure = { exception ->
                // Handle errors more specifically if needed
                Log.e(logTag, "Failed to process EML file: ${exception.message}")
            }
        )
    }

    fun resetSelectionMode() {
        _isSelectionMode.value = false
        _selectedEmails.value = emptySet()
    }

}
