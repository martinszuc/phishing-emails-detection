package com.martinszuc.phishing_emails_detection.ui.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    // Base class to handle common tasks, like error handling
    fun handleException(task: () -> Unit) {
        viewModelScope.launch {
            try {
                task()
            } catch (e: Exception) {
                Log.e("BaseViewModel", "Exception: ${e.message}")
            }
        }
    }
}
