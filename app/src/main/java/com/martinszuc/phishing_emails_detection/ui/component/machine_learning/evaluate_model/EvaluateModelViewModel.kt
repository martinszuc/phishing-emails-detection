package com.martinszuc.phishing_emails_detection.ui.component.machine_learning.evaluate_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.data.python.model.EvaluateModel
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class EvaluateModelViewModel @Inject constructor() : AbstractBaseViewModel() {
    private val _evaluationResults = MutableLiveData<String>()
    val evaluationResults: LiveData<String> = _evaluationResults

    fun initiateModelEvaluation(model: ModelMetadata) {
        launchDataLoad(
            execution = {
                EvaluateModel().evaluateModel(
                    model,
                    Constants.TESTING_DS_SAFE_FILENAME,
                    Constants.TESTING_DS_PHIS_FILENAME
                )
            },
            onSuccess = { result ->
                _evaluationResults.postValue(result)
            },
            onFailure = { exception ->
                // Handle failure if needed
            }
        )
    }
}