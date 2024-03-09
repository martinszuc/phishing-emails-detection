package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_processed_manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.processed_packages.ProcessedPackageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProcessedPackageManagerViewModel @Inject constructor(
    private val processedPackageRepository: ProcessedPackageRepository
) : ViewModel() {

    // LiveData to observe processed packages
    // Example: val processedPackages = MutableLiveData<List<ProcessedPackageMetadata>>()

    fun deleteProcessedPackage(fileName: String) {
        viewModelScope.launch {
            processedPackageRepository.deleteProcessedPackage(fileName)
            // Update LiveData
        }
    }

    // Add other methods as necessary, for example, to load processed packages metadata
}
