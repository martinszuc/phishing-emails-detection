package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detector

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.email_package.EmailPackageRepository
import com.martinszuc.phishing_emails_detection.data.email_package.PackageManifestManager
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
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
}
