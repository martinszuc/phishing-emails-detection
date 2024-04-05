package com.martinszuc.phishing_emails_detection.data.model

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.model_manager.ModelRepository
import com.martinszuc.phishing_emails_detection.data.python.PythonSingleton
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

class WeightManager @Inject constructor() {

    private val TAG = "FederatedWeightManager"

    fun extractModelWeights(modelName: String): String {
        Log.d(TAG, "Extracting weights for model at: $modelName")

        val python = PythonSingleton.instance
        val pythonScript = python.getModule("utils_weights")

        val weightsJson = pythonScript.callAttr("serialize_model_weights", modelName).toString()
        Log.d(TAG, "Weights extracted for $modelName")

        return weightsJson
    }

    fun updateModelWithNewWeights(modelName: String, weightsJson: String) {
        Log.d(TAG, "Updating model at: $modelName with new weights")

        val python = PythonSingleton.instance
        val pythonScript = python.getModule("utils_weights")

        pythonScript.callAttr("deserialize_and_load_model_weights", modelName, weightsJson)
        Log.d(TAG, "Model $modelName updated with new weights")
    }
}