package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_package_manager

import android.net.Uri
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.email_package.EmailPackageRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class EmailPackageManagerViewModel @Inject constructor(
    private val emailPackageRepository: EmailPackageRepository
) : AbstractBaseViewModel() {

    fun deleteEmailPackage(fileName: String) {
        launchDataLoad(execution = {
            emailPackageRepository.deleteEmailPackage(fileName)
        })
    }

    fun createAndSaveEmailPackageFromMboxFile(uri: Uri, isPhishy: Boolean, packageName: String) {
        launchDataLoad(execution = {
            emailPackageRepository.createAndSaveEmailPackageFromMbox(uri, isPhishy, packageName)
        })
    }

}
