package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.data_repository.auth.AuthenticationRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailFullLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailMboxLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.emails.EmailMinimalLocalRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.remote.repository.EmailFullRemoteRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import com.martinszuc.phishing_emails_detection.utils.emails.EmailFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val logTag = "EmailsImportViewModel"


/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class EmailsImportViewModel @Inject constructor(
    private val emailMinimalLocalRepository: EmailMinimalLocalRepository,
    private val emailFullLocalRepository: EmailFullLocalRepository,
    private val emailFullRemoteRepository: EmailFullRemoteRepository,
    private val emailMboxLocalRepository: EmailMboxLocalRepository,
    private val authenticationRepository: AuthenticationRepository,
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
        viewModelScope.launch(Dispatchers.IO) { // Use IO dispatcher for network/db operations
            Log.d(logTag, "Starting to import selected emails")
            val selectedEmailsList = selectedEmails.value ?: emptyList()

            withContext(Dispatchers.Main) { // Switch to Main thread to update UI components
                _totalCount.value = selectedEmailsList.size
                _progress.value = 0
                Log.d(logTag, "Total emails to import: ${selectedEmailsList.size}")
            }

            if (selectedEmailsList.isNotEmpty()) {
                Log.d(logTag, "Fetching full emails by IDs")
                val fullEmails =
                    emailFullRemoteRepository.getEmailsFullByIds(selectedEmailsList.map { it.id })

                fullEmails.forEachIndexed { index, email ->
                    Log.d(logTag, "Processing email ${index + 1} of ${fullEmails.size} with ID: ${email.id}")

                    // Database operations can remain on the IO dispatcher
                    emailFullLocalRepository.insertAllEmailsFull(listOf(email))
                    Log.d(logTag, "Inserted full email for ID: ${email.id}")

                    emailMboxLocalRepository.buildAndSaveMbox(email)
                    Log.d(logTag, "Built and saved mbox for email ID: ${email.id}")

                    withContext(Dispatchers.Main) { // Update progress on the main thread
                        _progress.value = index + 1
                        Log.d(logTag, "Updated progress to: ${_progress.value}")
                    }
                }

                withContext(Dispatchers.IO) {
                    emailMinimalLocalRepository.insertAll(selectedEmailsList)
                    Log.d(logTag, "Inserted all minimal email data")
                }
            } else {
                withContext(Dispatchers.Main) {
                    Log.e(logTag, "No emails selected")
                    throw Exception("No emails selected")
                }
            }
        }
    }


    fun fetchAndSaveEmailsBasedOnFilterAndLimit(query: String, limit: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(logTag, "Attempting to fetch and save emails with query: $query and limit: $limit")
            val account = authenticationRepository.getCurrentAccount()
            if (account != null) {
                Log.d(logTag, "Account retrieved successfully: ${account.email}")
                _totalCount.postValue(limit)
                _progress.postValue(0)

                val emails = emailFullRemoteRepository.fetchEmailsBasedOnFilterAndLimit(
                    account, query, limit,
                    progressCallback = { progress ->
                        Log.d(logTag, "Current fetch progress: $progress of $limit")
                        _progress.postValue(progress)  // Directly post progress updates
                    }
                )

                // Proceed with processing emails if any
                if (emails.isNotEmpty()) {
                    Log.d(logTag, "Fetched ${emails.size} emails, starting processing")
                    emails.forEachIndexed { index, emailFull ->
                        Log.d(logTag, "Processing email ${index + 1} of ${emails.size}: ${emailFull.id}")
                        emailFullLocalRepository.insertEmailFull(emailFull)
                        Log.d(logTag, "Inserted full email data for email ID: ${emailFull.id}")

                        val emailMinimal = EmailFactory.createEmailMinimalFromFull(emailFull)
                        emailMinimalLocalRepository.insert(emailMinimal)
                        Log.d(logTag, "Inserted minimal email data for email ID: ${emailFull.id}")

                        emailMboxLocalRepository.buildAndSaveMbox(emailFull)
                        Log.d(logTag, "Built and saved mbox for email ID: ${emailFull.id}")

                        _progress.postValue(index + 1)  // Continue to update progress correctly
                        Log.d(logTag, "Updated progress to ${index + 1}")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e(logTag, "No emails fetched with the given query and limit.")
                        throw Exception("No emails fetched")
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Log.e(logTag, "Google SignIn Account is null, aborting fetch and save process")
                    throw Exception("Google SignIn Account is null")
                }
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
                val range =
                    if (firstIndex < secondIndex) firstIndex..secondIndex else secondIndex..firstIndex
                addToSelectedEmails(_listOfEmailsBeforeSelection.value!!.slice(range))
                _isSelectionMode.value = false
                firstSelectedEmail = null
            }
        }
    }
}