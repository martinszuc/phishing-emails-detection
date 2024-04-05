package com.martinszuc.phishing_emails_detection.data.model

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.python.PythonSingleton
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

class Retraining @Inject constructor() {

    private val TAG = "ModelRetraining"

    fun retrainModel(resourcesDir: String, safeFilename: String, phishingFilename: String, modelName: String) {
        Log.d(TAG, "Starting model retraining")

        val python = PythonSingleton.instance
        Log.d(TAG, "Python instance obtained")

        // Assuming your Python retrain script is named 'model_retrain'
        val pythonScript = python.getModule("model_retrain")
        Log.d(TAG, "Python retrain script module loaded")

        pythonScript.callAttr(
            "main",
            resourcesDir,
            safeFilename,
            phishingFilename,
            modelName
        )
        Log.d(TAG, "Called retrain_model with parameters - resourcesDir: $resourcesDir, safeFilename: $safeFilename, phishingFilename: $phishingFilename, modelName: $modelName")
    }

}