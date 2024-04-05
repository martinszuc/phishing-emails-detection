package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailFullLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.remote.repository.EmailFullRemoteRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class EmailsImportViewModel @Inject constructor(
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
    private val emailFullLocalRepository: EmailFullLocalRepository,
    private val emailFullRemoteRepository: EmailFullRemoteRepository
) : AbstractBaseViewModel() {

    private val _isSelectionMode = MutableLiveData(false)
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    private val _listOfEmailsBeforeSelection = MutableLiveData<List<EmailMinimal>>(listOf())
    private val _selectedEmails = MutableLiveData<List<EmailMinimal>>(listOf())
    val selectedEmails: LiveData<List<EmailMinimal>> = _selectedEmails

    private var firstSelectedEmail: EmailMinimal? = null

    fun toggleEmailSelected(email: EmailMinimal) {
        val currentSelectedEmails = _selectedEmails.value ?: emptyList()
        _selectedEmails.value = if (email in currentSelectedEmails) {
            currentSelectedEmails - email
        } else {
            currentSelectedEmails + email
        }
    }

    fun importSelectedEmails() {
        launchDataLoad(execution = {
            val selectedEmailsList = selectedEmails.value ?: emptyList()
            if (selectedEmailsList.isNotEmpty()) {
                val fullEmails = emailFullRemoteRepository.getEmailsFullByIds(selectedEmailsList.map { it.id })
                emailFullLocalRepository.insertAllEmailsFull(fullEmails)
                selectedEmailsList.forEach { email ->
                    emailFullRemoteRepository.fetchAndSaveRawEmail(email.id)
                }
                emailMinimalLocalRepository.insertAll(selectedEmailsList)
            } else {
                throw Exception("No packages selected")
            }
        }, onFailure = { e ->
            Log.e("EmailsImportViewModel", "Error during import: ${e.message}")
        })
    }

    fun fetchAndSaveEmailsBasedOnFilterAndLimit(query: String, limit: Int) {
        launchDataLoad(execution = {
            withContext(Dispatchers.IO) {
                emailFullRemoteRepository.fetchAndSaveEmailsBasedOnFilterAndLimit(query, limit)
            }
        }, onFailure = { e ->
            Log.e("EmailsImportViewModel", "Error fetching and saving emails: ${e.message}")
        })
    }

    private fun addToSelectedEmails(selected: List<EmailMinimal>) {
        val currentSelected = _selectedEmails.value.orEmpty().toMutableList()
        selected.forEach { email ->
            if (!currentSelected.contains(email)) currentSelected.add(email)
        }
        _selectedEmails.value = currentSelected
    }

    fun handleFirstSelection(email: EmailMinimal, visibleEmails: List<EmailMinimal>) {
        firstSelectedEmail = email
        _listOfEmailsBeforeSelection.value = visibleEmails
        _isSelectionMode.value = true
    }

    fun handleSecondSelection(secondEmail: EmailMinimal) {
        firstSelectedEmail?.let { firstEmail ->
            val firstIndex = _listOfEmailsBeforeSelection.value?.indexOf(firstEmail) ?: -1
            val secondIndex = _listOfEmailsBeforeSelection.value?.indexOf(secondEmail) ?: -1
            if (firstIndex != -1 && secondIndex != -1) {
                val range = if (firstIndex < secondIndex) firstIndex..secondIndex else secondIndex..firstIndex
                addToSelectedEmails(_listOfEmailsBeforeSelection.value!!.slice(range))
                _isSelectionMode.value = false
                firstSelectedEmail = null
            }
        }
    }
}