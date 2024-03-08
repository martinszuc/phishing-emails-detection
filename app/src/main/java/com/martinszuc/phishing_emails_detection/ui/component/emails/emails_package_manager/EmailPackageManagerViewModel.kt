package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_package_manager

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.email_package.EmailPackageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
@HiltViewModel
class EmailPackageManagerViewModel @Inject constructor(
    private val emailPackageRepository: EmailPackageRepository
) : ViewModel() {

    fun deleteEmailPackage(fileName: String) {
        viewModelScope.launch {
            emailPackageRepository.deleteEmailPackage(fileName)
        }
    }

    fun createAndSaveEmailPackageFromMboxFile(uri: Uri, isPhishy: Boolean, packageName: String) {
        viewModelScope.launch {
            try {
                emailPackageRepository.createAndSaveEmailPackageFromMbox(uri, isPhishy, packageName)
            } catch (e: Exception) {
                // Handle any errors
            }
        }
    }

}
