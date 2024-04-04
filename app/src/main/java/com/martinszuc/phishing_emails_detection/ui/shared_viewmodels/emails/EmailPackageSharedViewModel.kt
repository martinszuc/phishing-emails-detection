package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.email_package.EmailPackageRepository
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

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
