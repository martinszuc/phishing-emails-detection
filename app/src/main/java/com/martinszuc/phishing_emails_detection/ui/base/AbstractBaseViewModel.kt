package com.martinszuc.phishing_emails_detection.ui.base

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.log

abstract class AbstractBaseViewModel : ViewModel() {

    protected val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    protected val _isFinished = MutableLiveData(false)
    val isFinished: LiveData<Boolean> = _isFinished

    protected val _operationFailed = MutableLiveData(false)
    val operationFailed: LiveData<Boolean> = _operationFailed

    protected val _hasStarted = MutableLiveData(false)
    val hasStarted: LiveData<Boolean> = _hasStarted

    private val logTag = "BaseViewModel"

    protected fun <T> launchDataLoad(
        execution: suspend () -> T,
        onSuccess: (T) -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        _hasStarted.postValue(true) // Indicate that the operation has started
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val result = execution()
                onSuccess(result)
                _isFinished.postValue(true)
            } catch (e: Exception) {
                Log.e(logTag, "Error in launchDataLoad: ${e.message}")
                onFailure(e)
                _operationFailed.postValue(true)
            } finally {
                _isLoading.postValue(false)
                _hasStarted.postValue(false) // Reset hasStarted when the operation is complete
            }
        }
    }

    protected fun <T> collectFlow(
        flow: Flow<T>,
        dispatcher: CoroutineDispatcher = Dispatchers.Main, // Flow collection typically on Main dispatcher
        onEach: (T) -> Unit
    ) {
        _hasStarted.postValue(true)
        _isLoading.postValue(true)
        viewModelScope.launch(dispatcher) {
            try {
                flow.collectLatest { data ->
                    onEach(data)
                }
                _isFinished.postValue(true)
            } catch (e: Exception) {
                Log.e(logTag, "Error collecting flow: ${e.message}")
                _operationFailed.postValue(true)
            } finally {
                _isLoading.postValue(false)
                _hasStarted.postValue(false)
            }
        }
    }

    fun clearStates() {
        _isFinished.postValue(false)
        _operationFailed.postValue(false)
        _hasStarted.postValue(false) // Ensure to reset this as well when clearing states
    }

    // Optional: Provide separate clear methods for more granular control
    fun clearIsFinished() {
        _isFinished.postValue(false)
    }

    fun clearOperationFailed() {
        _operationFailed.postValue(false)
    }

    // Additional method to reset hasStarted state
    fun clearHasStarted() {
        _hasStarted.postValue(false)
    }

    fun showToast(message: String, context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}
