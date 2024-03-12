package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.email_package.EmailPackageRepository
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.data.file.FileRepository
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailPackageSharedViewModel @Inject constructor(
    private val emailPackageRepository: EmailPackageRepository,
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _emailPackages = MutableLiveData<List<EmailPackageMetadata>>()
    val emailPackages: LiveData<List<EmailPackageMetadata>> = _emailPackages

    init {
        loadEmailPackages()
    }

    fun loadEmailPackages() {
        viewModelScope.launch {
            val packages = emailPackageRepository.loadEmailPackagesMetadata()
            _emailPackages.postValue(packages)
        }
    }

}