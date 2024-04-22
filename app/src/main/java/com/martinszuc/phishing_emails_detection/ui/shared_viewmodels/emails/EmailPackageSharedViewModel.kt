package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.email_package.EmailPackageRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.file.FileRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class EmailPackageSharedViewModel @Inject constructor(
    private val emailPackageRepository: EmailPackageRepository,
    private val fileRepository: FileRepository
) : AbstractBaseViewModel() { // Extend AbstractBaseViewModel

    private val _emailPackages = MutableLiveData<List<EmailPackageMetadata>>()
    val emailPackages: LiveData<List<EmailPackageMetadata>> = _emailPackages

    init {
        loadEmailPackages()
    }

    fun loadEmailPackages() {
        launchDataLoad(
            execution = { emailPackageRepository.loadEmailPackagesMetadata() },
            onSuccess = { packages -> _emailPackages.postValue(packages) }
        )
    }
}
