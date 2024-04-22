package com.martinszuc.phishing_emails_detection.ui.component.data_picking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class DataPickingViewModel @Inject constructor(
) : ViewModel() {

    private val _selectedPackages = MutableLiveData<Set<EmailPackageMetadata>>(setOf())
    val selectedPackages: LiveData<Set<EmailPackageMetadata>> = _selectedPackages

    fun togglePackageSelected(packageMetadata: EmailPackageMetadata) {
        val currentSelectedPackages = _selectedPackages.value.orEmpty()
        _selectedPackages.value = if (currentSelectedPackages.any { it.fileName == packageMetadata.fileName }) {
            currentSelectedPackages.filter { it.fileName != packageMetadata.fileName }.toSet()
        } else {
            currentSelectedPackages + packageMetadata
        }
    }

    fun clearSelectedPackages() {
        _selectedPackages.value = setOf()
    }
}
