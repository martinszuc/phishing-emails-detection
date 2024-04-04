package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_processed_manager

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.processed_packages.ProcessedPackageRepository
import com.martinszuc.phishing_emails_detection.data.processed_packages.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProcessedPackageManagerViewModel @Inject constructor(
    private val processedPackageRepository: ProcessedPackageRepository,
) : AbstractBaseViewModel() { // Extend AbstractBaseViewModel

    val processedPackages = MutableLiveData<List<ProcessedPackageMetadata>>()

    fun deleteProcessedPackage(fileName: String) {
        launchDataLoad(execution = {
            processedPackageRepository.deleteProcessedPackage(fileName)
            // Potentially update processedPackages LiveData here
        }, onSuccess = {
            // Optionally handle success, e.g., by refreshing the list of processed packages
        }, onFailure = { e ->
            // Handle any errors
        })
    }

    fun createAndSaveProcessedPackageFromCsvFile(uri: Uri, isPhishy: Boolean, packageName: String) {
        launchDataLoad(execution = {
            processedPackageRepository.createAndAddProcessedPackageFromCsv(
                uri,
                isPhishy,
                packageName
            )
            // Optionally update LiveData or perform additional actions after successful creation
        }, onSuccess = {
            // Optionally handle success, e.g., by updating LiveData or notifying the user
        }, onFailure = { e ->
            // Handle any errors
        })
    }

    // Implement other methods as necessary, for example, to load processed packages metadata
    fun loadProcessedPackages() {
        launchDataLoad(execution = {
            processedPackageRepository.loadProcessedPackagesMetadata()
        }, onSuccess = { packages ->
            processedPackages.postValue(packages)
        }, onFailure = { e ->
            // Handle errors, possibly updating a LiveData to notify the UI
        })
    }
}
