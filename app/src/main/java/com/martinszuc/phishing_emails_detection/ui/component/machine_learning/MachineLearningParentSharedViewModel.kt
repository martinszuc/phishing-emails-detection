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
    fun decideNextStateBasedOnCondition(condition: Boolean) {
        if (condition) {
            setState(MachineLearningState.TRAINING)
        } else {
            setState(MachineLearningState.RETRAINING)
        }
    }

}
enum class MachineLearningState {
    DATA_PICKING,
    DATA_PROCESSING,
    TRAINING,
    RETRAINING
}

