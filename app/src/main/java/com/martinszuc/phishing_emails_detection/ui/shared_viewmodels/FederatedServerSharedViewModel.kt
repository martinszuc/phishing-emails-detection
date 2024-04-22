package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.data_repository.remote.network.retrofit.ModelWeightsService
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val logTag = "FederatedServerSharedViewModel"

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class FederatedServerSharedViewModel @Inject constructor(
    private val modelWeightsService: ModelWeightsService // Ensure this is provided through DI correctly
) : AbstractBaseViewModel() {

    private val _isServerOperational = MutableLiveData<Boolean>()
    val isServerOperational: LiveData<Boolean> = _isServerOperational

    fun checkServerConnection() {
        _hasStarted.value = true // Indicate operation has started
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = modelWeightsService.checkServer()
                if (response.isSuccessful) {
                    // Post success status on the main thread
                    _isServerOperational.postValue(true)
                } else {
                    // Post failure status on the main thread
                    _isServerOperational.postValue(false)
                }
            } catch (e: Exception) {
                Log.e(logTag, "Error checking server connection: ${e.localizedMessage}")
                _isServerOperational.postValue(false)
            } finally {
                _isLoading.postValue(false) // Indicate loading has finished
                _hasStarted.postValue(false) // Reset operation started state
            }
        }
    }
}
