package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martinszuc.phishing_emails_detection.data.model_manager.ModelManager
import com.martinszuc.phishing_emails_detection.data.model_manager.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.data.model_manager.ModelRepository
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

    private val _selectedModel = MutableLiveData<ModelMetadata>()
    val selectedModel: LiveData<ModelMetadata> = _selectedModel

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

    fun toggleSelectedModel(modelMetadata: ModelMetadata) {
        val currentSelectedModel = _selectedModel.value
        if (currentSelectedModel == modelMetadata) {
            // If the same model is selected again, deselect it
//            _selectedModel.value = null
        } else {
            // Select the new model
            _selectedModel.value = modelMetadata
        }
    }
}

