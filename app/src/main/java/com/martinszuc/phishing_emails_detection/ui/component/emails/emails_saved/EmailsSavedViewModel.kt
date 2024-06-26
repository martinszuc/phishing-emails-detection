package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailDetectionLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailFullLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailMboxLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.email_package.EmailPackageRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val logTag = "EmailsImportViewModel"

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class EmailsSavedViewModel @Inject constructor(
    private val emailFullLocalRepository: EmailFullLocalRepository,
    private val emailPackageRepository: EmailPackageRepository,
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
    private val emailMboxLocalRepository: EmailMboxLocalRepository,
    private val emailDetectionLocalRepository: EmailDetectionLocalRepository
) : AbstractBaseViewModel() {

    private val _isSelectionMode = MutableLiveData(false)
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

            val range =
                if (firstIndex < secondIndex) firstIndex..secondIndex else secondIndex..firstIndex
            val rangeIds = visibleEmailIds.slice(range).toSet()

            addToSelectedEmails(rangeIds)

            // Reset selection mode
            _isSelectionMode.value = false
            firstSelectedEmailId = null
        }
    }

    fun createEmailFullFromEML(uri: Uri) {
        launchDataLoad(
            execution = {
                emailFullLocalRepository.saveEmlToEmailFull(uri)
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

    private fun addToSelectedEmails(selectedIds: Set<String>) {
        val currentSelected = _selectedEmails.value.orEmpty().toMutableSet()
        currentSelected.addAll(selectedIds)
        _selectedEmails.value = currentSelected
    }

    fun clearDatabase() {
        launchDataLoad(execution = {
            emailFullLocalRepository.clearAll()
            emailMinimalLocalRepository.deleteAllEmails()
        })
    }

    fun createEmailPackageFromSelected(isPhishy: Boolean, packageName: String) {
        val emailIds = _selectedEmails.value?.toList() ?: return
        launchDataLoad(execution = {
            emailPackageRepository.createEmailPackage(
                emailIds,
                isPhishy,
                packageName,
                progressCallback = { progress ->
                    _progress.postValue(progress)  // Safely update progress on UI thread
                })
        }, onSuccess = {
            deleteEmails(emailIds, progressCallback = { progress ->
                _progress.postValue(progress)  // Safely update progress on UI thread
            })
        })
    }

    fun createEmailPackageFromLatest(isPhishy: Boolean, packageName: String, limit: Int) {
        viewModelScope.launch {
            val latestEmailIds = emailMinimalLocalRepository.fetchLatestEmailIds(limit)
            if (latestEmailIds.isNotEmpty()) {
                _totalCount.postValue(latestEmailIds.size)  // Update the total count for progress tracking

                launchDataLoad(execution = {
                    // Perform the package creation on an IO dispatcher
                    emailPackageRepository.createEmailPackage(
                        latestEmailIds,
                        isPhishy,
                        packageName,
                        progressCallback = { progress ->
                            _progress.postValue(progress)  // Update progress safely on the UI thread
                        }
                    )
                }, onSuccess = {
                    _progress.postValue(0)
                    // After successfully creating the package, delete the emails
                    deleteEmails(latestEmailIds, progressCallback = { progress ->
                        _progress.postValue(progress)  // Update progress safely on the UI thread
                    })

                }, onFailure = { e ->
                    Log.e(logTag, "Error during package creation: ${e.message}")
                })
            } else {
                Log.e(logTag, "No latest emails found to package")
            }
        }
    }

    private fun deleteEmails(emailIds: List<String>, progressCallback: (Int) -> Unit) {
        launchDataLoad(execution = {
            emailIds.forEachIndexed { index, emailId ->
                if (!emailDetectionLocalRepository.isEmailSaved(emailId)) {
                    emailFullLocalRepository.deleteEmailById(emailId)
                    emailMinimalLocalRepository.deleteEmailById(emailId)
                    emailMboxLocalRepository.deleteEmailMboxById(emailId)
                    progressCallback(index + 1)
                } else {
                    emailFullLocalRepository.deleteEmailById(emailId)
                    progressCallback(index + 1)
                    Log.d(logTag, "Skipped deletion for saved email with ID: $emailId")
                }
            }
        }, onSuccess = {
            Log.d(logTag, "Emails deleted successfully.")
            _progress.postValue(0)
        }, onFailure = { e ->
            Log.e(logTag, "Failed to delete emails: ${e.message}")
        })
    }

    fun resetSelectionMode() {
        _isSelectionMode.value = false
        _selectedEmails.value = emptySet()
        firstSelectedEmailId = null
        _listOfEmailsBeforeSelection.value = listOf()
    }

}
