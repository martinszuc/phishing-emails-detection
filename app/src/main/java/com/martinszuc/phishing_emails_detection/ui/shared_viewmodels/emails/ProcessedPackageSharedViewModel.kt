package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.processed_packages.ProcessedPackageRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class ProcessedPackageSharedViewModel @Inject constructor(
    private val processedPackageRepository: ProcessedPackageRepository,
) : AbstractBaseViewModel() {

    private val _processedPackages = MutableLiveData<List<ProcessedPackageMetadata>>()
    val processedPackages: LiveData<List<ProcessedPackageMetadata>> = _processedPackages

    init {
        refreshAndLoadProcessedPackages()
    }

    fun refreshAndLoadProcessedPackages() {
        launchDataLoad(
            execution = {
                processedPackageRepository.refreshProcessedPackagesFromDir()
                processedPackageRepository.loadProcessedPackagesMetadata()
            },
            onSuccess = { packages -> _processedPackages.postValue(packages) }
        )
    }
}
