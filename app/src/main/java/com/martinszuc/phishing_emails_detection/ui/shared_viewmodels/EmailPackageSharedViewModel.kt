package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels

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

    fun getPackageFilePathsByNames(packageNames: List<String>): LiveData<List<String>> {
        val filePathsLiveData = MutableLiveData<List<String>>()

        viewModelScope.launch {
            // Fetch all available package metadata
            val allPackages = emailPackageRepository.loadEmailPackagesMetadata()

            // Filter packages whose names are in the provided list
            val selectedPackages = allPackages.filter { it.packageName in packageNames }

            // Resolve each selected package's file name to a file path
            val filePaths = selectedPackages.mapNotNull { packageMeta ->
                // Assuming you have a method in FileRepository to get the actual file path
                fileRepository.getFilePath(Constants.DIR_EMAIL_PACKAGES, packageMeta.fileName)
            }

            // Post the list of file paths to the LiveData object
            filePathsLiveData.postValue(filePaths)
        }

        return filePathsLiveData
    }

}