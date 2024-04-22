package com.martinszuc.phishing_emails_detection.data.model

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.python.PythonSingleton
import javax.inject.Inject

private const val logTag = "ModelRetrainingModule"

/**
 * @author matoszuc@gmail.com
 */

class Retraining @Inject constructor() {

    fun retrainModel(resourcesDir: String, safeFilename: String, phishingFilename: String, modelName: String) {
        Log.d(logTag, "Starting model retraining")

        val python = PythonSingleton.instance
        Log.d(logTag, "Python instance obtained")

        // Assuming your Python retrain script is named 'model_retrain'
        val pythonScript = python.getModule("model_retrain")
        Log.d(logTag, "Python retrain script module loaded")

        pythonScript.callAttr(
            "main",
            resourcesDir,
            safeFilename,
            phishingFilename,
            modelName
        )
        Log.d(logTag, "Called retrain_model with parameters - resourcesDir: $resourcesDir, safeFilename: $safeFilename, phishingFilename: $phishingFilename, modelName: $modelName")
    }

}