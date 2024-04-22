package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.data_repository.local.component.model_manager.ModelRepository
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author matoszuc@gmail.com
 */

@HiltViewModel
class ModelManagerSharedViewModel @Inject constructor(
    private val modelRepository: ModelRepository
) : AbstractBaseViewModel() {

    private val _models = MutableLiveData<List<ModelMetadata>>()
    val models: LiveData<List<ModelMetadata>> = _models

    init {
        refreshAndLoadModels()
    }

    fun refreshAndLoadModels() {
        launchDataLoad(
            execution = {
                modelRepository.refreshModelsFromDir()
                modelRepository.loadModelsMetadata()
            },
            onSuccess = { models -> _models.postValue(models) }
        )
    }
}
