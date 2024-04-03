package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EmailParentSharedViewModel @Inject constructor() : ViewModel() {

    private val _viewPagerPosition = MutableLiveData<Int>()
    val viewPagerPosition: LiveData<Int> = _viewPagerPosition

    fun setViewPagerPosition(position: Int) {
        _viewPagerPosition.value = position
    }
}