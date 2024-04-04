package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EmailParentSharedViewModel @Inject constructor() : AbstractBaseViewModel() {

    private val _viewPagerPosition = MutableLiveData<Int>()
    val viewPagerPosition: LiveData<Int> = _viewPagerPosition

    fun setViewPagerPosition(position: Int) {
        _viewPagerPosition.value = position
    }
}