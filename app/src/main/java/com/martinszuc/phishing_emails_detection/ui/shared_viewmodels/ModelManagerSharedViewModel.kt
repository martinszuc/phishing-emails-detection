package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.model_manager.ModelManager
import com.martinszuc.phishing_emails_detection.data.model_manager.ModelMetadata
import com.martinszuc.phishing_emails_detection.data.model_manager.ModelRepository
import com.martinszuc.phishing_emails_detection.data.processed_packages.entity.ProcessedPackageMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelManagerSharedViewModel @Inject constructor(
    private val modelManager: ModelManager,
    private val modelRepository: ModelRepository
    ) : ViewModel() {

    private val _models = MutableLiveData<List<ModelMetadata>>()
    val models: LiveData<List<ModelMetadata>> = _models


    init {
        refreshAndLoadModels()
    }

    fun refreshAndLoadModels() {
        viewModelScope.launch {
            modelManager.refreshModelsFromDir()
            val models = modelRepository.loadModelsMetadata()
            _models.postValue(models)
        }
    }
}

