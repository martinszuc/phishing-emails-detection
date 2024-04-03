package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.processed_packages.ProcessedPackageRepository
import com.martinszuc.phishing_emails_detection.data.processed_packages.entity.ProcessedPackageMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProcessedPackageSharedViewModel @Inject constructor(
    private val processedPackageRepository: ProcessedPackageRepository,
) : ViewModel() {

    private val _processedPackages = MutableLiveData<List<ProcessedPackageMetadata>>()
    val processedPackages: LiveData<List<ProcessedPackageMetadata>> = _processedPackages

    init {
        refreshAndLoadProcessedPackages()
    }

    fun refreshAndLoadProcessedPackages() {
        viewModelScope.launch {
            processedPackageRepository.refreshProcessedPackages()

            // After refreshing the manifest, load the processed packages metadata
            val packages = processedPackageRepository.loadProcessedPackagesMetadata()
            _processedPackages.postValue(packages)
        }
    }

}