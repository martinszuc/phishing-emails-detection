package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailFullLocalRepository
import com.martinszuc.phishing_emails_detection.data.email_package.EmailPackageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class EmailsSavedViewModel @Inject constructor(
    private val emailFullLocalRepository: EmailFullLocalRepository,
    private val emailPackageRepository: EmailPackageRepository
) : ViewModel() {

    private val _isSelectionMode = MutableLiveData<Boolean>(false)
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    private val _listOfEmailsBeforeSelection = MutableLiveData<List<String>>(listOf())

    private val _selectedEmails = MutableLiveData<Set<String>>(setOf())
    val selectedEmails: LiveData<Set<String>> = _selectedEmails

    private var firstSelectedEmailId: String? = null

    fun toggleEmailSelected(id: String) {
        val currentSelectedEmails = _selectedEmails.value ?: emptySet()
        _selectedEmails.value = currentSelectedEmails.toMutableSet().apply {
            if (contains(id)) remove(id) else add(id)
        }
    }

    fun handleFirstSelection(emailId: String, visibleEmailIds: List<String>) {
        firstSelectedEmailId = emailId
        _listOfEmailsBeforeSelection.value = visibleEmailIds
        _isSelectionMode.value = true
    }

    fun handleSecondSelection(secondEmailId: String) {
        firstSelectedEmailId?.let { firstId ->
            val visibleEmailIds = _listOfEmailsBeforeSelection.value ?: listOf()
            val firstIndex = visibleEmailIds.indexOf(firstId)
            val secondIndex = visibleEmailIds.indexOf(secondEmailId)
            if (firstIndex == -1 || secondIndex == -1) return

            val range = if (firstIndex < secondIndex) firstIndex..secondIndex else secondIndex..firstIndex
            val rangeIds = visibleEmailIds.slice(range).toSet()

            addToSelectedEmails(rangeIds)

            // Reset selection mode
            _isSelectionMode.value = false
            firstSelectedEmailId = null
        }
    }

    private fun addToSelectedEmails(selectedIds: Set<String>) {
        val currentSelected = _selectedEmails.value.orEmpty().toMutableSet()
        currentSelected.addAll(selectedIds)
        _selectedEmails.value = currentSelected
    }

    fun clearDatabase() {
        viewModelScope.launch {
            emailFullLocalRepository.clearAll()
        }
    }
    fun loadSelectedEmailPackageContent(fileName: String) {
        viewModelScope.launch {
            val packageContent = emailPackageRepository.loadEmailPackageContent(fileName)
            packageContent?.let {
                // Update the UI with the content or process it as needed
            } ?: run {
                // Handle the case where the file does not exist or an error occurred
            }
        }
    }

    fun createEmailPackageFromSelected(isPhishy: Boolean, packageName: String) {
        // Assuming _selectedEmails holds the list of email IDs to be packaged
        val emailIds = _selectedEmails.value?.toList() ?: return

        viewModelScope.launch {
            try {
                val filePath = emailPackageRepository.createEmailPackage(emailIds, isPhishy, packageName)

            } catch (e: Exception) {
                // Handle any errors that might occur during the package creation process.
            }
        }
    }

    fun resetSelectionMode() {
        _isSelectionMode.value = false
        _selectedEmails.value = emptySet()
        firstSelectedEmailId = null
        _listOfEmailsBeforeSelection.value = listOf()
    }

}
