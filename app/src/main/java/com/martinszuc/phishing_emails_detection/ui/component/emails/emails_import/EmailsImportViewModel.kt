package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailFullLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.email.remote.repository.EmailFullRemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    private val _isSelectionMode = MutableLiveData<Boolean>(false)
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    private val _listOfEmailsBeforeSelection = MutableLiveData<List<EmailMinimal>>(listOf())
    private val _selectedEmails = MutableLiveData<List<EmailMinimal>>(listOf())
    val selectedEmails: LiveData<List<EmailMinimal>> = _selectedEmails

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _operationFinished = MutableLiveData<Boolean>(false)
    val operationFinished: LiveData<Boolean> = _operationFinished

    private val _operationFailed = MutableLiveData<Boolean>(false)
    val operationFailed: LiveData<Boolean> = _operationFailed

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
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val selectedEmailsList = selectedEmails.value ?: emptyList()
                if (selectedEmailsList.isNotEmpty()) {
                    val fullEmails = emailFullRemoteRepository.getEmailsFullByIds(selectedEmailsList.map { it.id })
                    emailFullLocalRepository.insertAllEmailsFull(fullEmails)
                    selectedEmailsList.forEach { email ->
                        emailFullRemoteRepository.fetchAndSaveRawEmail(email.id)
                    }
                    emailMinimalLocalRepository.insertAll(selectedEmailsList)
                    _operationFinished.postValue(true)
                } else {
                    _operationFailed.postValue(true)
                }
            } catch (e: Exception) {
                Log.e("EmailsImportViewModel", "Error during import: ${e.message}")
                _operationFailed.postValue(true)
            } finally {
                _isLoading.postValue(false)
                _selectedEmails.postValue(emptyList())
            }
        }
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

    fun fetchAndSaveEmailsBasedOnFilterAndLimit(query: String, limit: Int) {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                emailFullRemoteRepository.fetchAndSaveEmailsBasedOnFilterAndLimit(query, limit)
                _operationFinished.postValue(true)
            } catch (e: Exception) {
                Log.e("EmailsImportViewModel", "Error fetching and saving emails: ${e.message}")
                _operationFailed.postValue(true)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}