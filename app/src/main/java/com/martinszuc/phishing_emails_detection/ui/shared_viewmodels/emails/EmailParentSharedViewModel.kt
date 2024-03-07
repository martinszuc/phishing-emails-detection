package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.email_package.EmailPackageRepository
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailParentSharedViewModel @Inject constructor() : ViewModel() {

    private val _viewPagerPosition = MutableLiveData<Int>()
    val viewPagerPosition: LiveData<Int> = _viewPagerPosition

    fun setViewPagerPosition(position: Int) {
        _viewPagerPosition.value = position
    }
}