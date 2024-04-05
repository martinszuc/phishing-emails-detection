package com.martinszuc.phishing_emails_detection.ui.component.model_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.model_manager.ModelRepository
import com.martinszuc.phishing_emails_detection.data.model_manager.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ModelManagerViewModel @Inject constructor(
    private val modelRepository: ModelRepository
) : AbstractBaseViewModel() {

    private val _selectedModel = MutableLiveData<ModelMetadata>()
    val selectedModel: LiveData<ModelMetadata> = _selectedModel

    fun toggleSelectedModel(modelMetadata: ModelMetadata) {
        _selectedModel.value = if (_selectedModel.value == modelMetadata) null else modelMetadata
    }
}