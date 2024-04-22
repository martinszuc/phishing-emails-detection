package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.processed_packages.ProcessedPackageRepository
import com.martinszuc.phishing_emails_detection.data.processed_packages.entity.ProcessedPackageMetadata
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
