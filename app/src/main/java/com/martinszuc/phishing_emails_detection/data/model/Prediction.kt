package com.martinszuc.phishing_emails_detection.data.model

import android.content.Context
import android.util.Log
import com.martinszuc.phishing_emails_detection.data.python.PythonSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Prediction @Inject constructor(private val context: Context) {

    private val TAG = "EmailPrediction"

    init {
        Log.d(TAG, "Prediction module is initializing")
        // Consider initializing Python here if needed, but it's likely already initialized by PythonSingleton
    }

    suspend fun classify(modelPath: String, mboxFileName: String): List<Float> = withContext(Dispatchers.IO) {
        try {
            val python = PythonSingleton.instance
            val pyModule = python.getModule("model_predict")

            // Call the Python function and get the predictions
            val predictions = pyModule.callAttr("predict_on_mbox", modelPath, mboxFileName).asList()

            // Flatten the list of lists and convert each prediction to Float
            val flatPredictions = predictions.flatMap { it.asList() }.map { it.toFloat() }

            Log.d(TAG, "Classification completed successfully")
            flatPredictions
        } catch (e: Exception) {
            Log.e(TAG, "Error during classification: ${e.message}", e)
            emptyList()
        }
    }
}
