package com.martinszuc.phishing_emails_detection.ui.component.data_picking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DataPickingViewModel @Inject constructor(
) : ViewModel() {

    private val _selectedPackages = MutableLiveData<Set<String>>(setOf())
    val selectedPackages: LiveData<Set<String>> = _selectedPackages

    fun togglePackageSelected(packageName: String) {
        val currentSelectedPackages = _selectedPackages.value.orEmpty()
        _selectedPackages.value = if (packageName in currentSelectedPackages) {
            currentSelectedPackages - packageName
        } else {
            currentSelectedPackages + packageName
        }
    }
}