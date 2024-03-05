package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailFullLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.remote.repository.EmailFullRemoteRepository
import com.martinszuc.phishing_emails_detection.data.email.remote.repository.EmailMinimalRemoteRepository
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailMinimalSharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class EmailsImportViewModel @Inject constructor(
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
    private val emailFullLocalRepository: EmailFullLocalRepository,
    private val emailFullRemoteRepository: EmailFullRemoteRepository
) : ViewModel() {

    val selectedEmails = MutableLiveData<List<EmailMinimal>>(emptyList())

    fun toggleEmailSelected(email: EmailMinimal) {
        val currentSelectedEmails = selectedEmails.value ?: emptyList()
        if (email in currentSelectedEmails) {
            selectedEmails.value = currentSelectedEmails - email
        } else {
            selectedEmails.value = currentSelectedEmails + email
        }
    }

    fun importSelectedEmails() {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedEmailsList = selectedEmails.value ?: return@launch

            // Fetch the full format of the selected emails
            val fullEmails =
                emailFullRemoteRepository.getEmailsFullByIds(selectedEmailsList.map { it.id })
            emailFullLocalRepository.insertAllEmailsFull(fullEmails)

            // Fetch and save the raw format of the selected emails
            selectedEmailsList.forEach { email ->
                emailFullRemoteRepository.fetchAndSaveRawEmail(email.id)
            }

            // Also save the minimal emails to the local database
            emailMinimalLocalRepository.insertAll(selectedEmailsList)

            // Clear the selected emails
            selectedEmails.postValue(emptyList())
        }
    }
}
