package com.martinszuc.phishing_emails_detection.ui.component.machine_learning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class MachineLearningParentSharedViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableLiveData<MachineLearningState>()
    val state: LiveData<MachineLearningState> = _state

    fun setState(newState: MachineLearningState) {
        _state.value = newState
    }
}


