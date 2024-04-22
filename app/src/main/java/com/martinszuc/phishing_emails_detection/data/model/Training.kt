package com.martinszuc.phishing_emails_detection.data.model

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.python.PythonSingleton
import javax.inject.Inject

private const val logTag = "ModelTrainingModule"


/**
 * @author matoszuc@gmail.com
 */

class Training @Inject constructor() {

    fun trainModel(resourcesDir: String, safeFilename: String, phishingFilename: String, modelSaveDir: String) {
        Log.d(logTag, "Starting model training")

        val python = PythonSingleton.instance
        Log.d(logTag, "Python instance obtained")

        // Assuming your Python script is named 'train_model_script' and placed in the correct location
        val pythonScript = python.getModule("model_train")
        Log.d(logTag, "Python script module loaded")

        pythonScript.callAttr(
            "train_and_evaluate_model",
            resourcesDir,
            safeFilename,
            phishingFilename,
            modelSaveDir
        )
        Log.d(logTag, "Called train_and_evaluate_model with parameters - resourcesDir: $resourcesDir, safeFilename: $safeFilename, phishingFilename: $phishingFilename, modelSaveDir: $modelSaveDir")
    }
}
