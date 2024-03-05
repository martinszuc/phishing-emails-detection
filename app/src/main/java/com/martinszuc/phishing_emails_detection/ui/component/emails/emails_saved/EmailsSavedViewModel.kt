package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailFullLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class EmailsSavedViewModel @Inject constructor(
    private val emailFullLocalRepository: EmailFullLocalRepository
) : ViewModel() {

    fun clearDatabase() {
        viewModelScope.launch {
            emailFullLocalRepository.clearAll()
        }
    }
}

