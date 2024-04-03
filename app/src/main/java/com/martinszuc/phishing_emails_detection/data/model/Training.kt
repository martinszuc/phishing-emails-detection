package com.martinszuc.phishing_emails_detection.data.model

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.python.PythonSingleton
import javax.inject.Inject

class Training @Inject constructor() {

    private val TAG = "ModelTraining"

    fun trainModel(resourcesDir: String, safeFilename: String, phishingFilename: String, modelSaveDir: String) {
        Log.d(TAG, "Starting model training")

        val python = PythonSingleton.instance
        Log.d(TAG, "Python instance obtained")

        // Assuming your Python script is named 'train_model_script' and placed in the correct location
        val pythonScript = python.getModule("model_train")
        Log.d(TAG, "Python script module loaded")

        pythonScript.callAttr(
            "train_and_evaluate_model",
            resourcesDir,
            safeFilename,
            phishingFilename,
            modelSaveDir
        )
        Log.d(TAG, "Called train_and_evaluate_model with parameters - resourcesDir: $resourcesDir, safeFilename: $safeFilename, phishingFilename: $phishingFilename, modelSaveDir: $modelSaveDir")
    }
}
