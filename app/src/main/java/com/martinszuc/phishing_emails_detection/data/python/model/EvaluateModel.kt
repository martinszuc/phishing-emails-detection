package com.martinszuc.phishing_emails_detection.data.python.model

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.ModelMetadata
import com.martinszuc.phishing_emails_detection.data.python.PythonSingleton
import javax.inject.Inject

private const val logTag = "EvaluateModel"

/**
 * Authored by matoszuc@gmail.com
 */

class EvaluateModel @Inject constructor() {
    fun evaluateModel(modelMetadata: ModelMetadata, phishingFilePath: String, safeFilePath: String): String {
        Log.d(logTag, "Starting model evaluation")

        val python = PythonSingleton.instance
        Log.d(logTag, "Python instance obtained")

        // Correct module name if different. Assuming it is stored as 'model_evaluation' script.
        val pythonScript = python.getModule("model_evaluation")
        Log.d(logTag, "Python script module loaded")

        // Make sure to pass the model directory name, phishing file path, and safe file path
        val evaluationResults = pythonScript.callAttr(
            "evaluate_model",
            modelMetadata.modelName,  // Assuming modelName is the directory name
            phishingFilePath,
            safeFilePath
        )

        val resultsString = evaluationResults.toString()  // Convert the Python object to a string
        Log.d(logTag, "Model evaluated. Results: $resultsString")

        return resultsString  // Return the evaluation results as a string
    }
}