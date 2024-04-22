package com.martinszuc.phishing_emails_detection.data.python.model

import android.content.Context
import android.util.Log
import com.martinszuc.phishing_emails_detection.data.python.PythonSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val logTag = "PredictionModule"

/**
 * Handles email classification tasks by interfacing with a Python module for model prediction.
 * This class provides a method to classify emails using a trained machine learning model, leveraging
 * a Python backend for prediction. The `classify` method asynchronously executes the prediction
 * process on the provided mbox file using the specified model path.
 *
 * @author matoszuc@gmail.com
 */
class Prediction @Inject constructor() {

    /**
     * Asynchronously classifies emails using a machine learning model and a Python backend.
     * This method calls a Python function to predict the classification of emails in the mbox file
     * using the provided model path. It returns a list of predicted probabilities for each class.
     *
     * @param modelPath The path to the trained machine learning model.
     * @param mboxFileName The name of the mbox file containing the emails to classify.
     * @return A list of Float values representing the predicted probabilities for each class.
     */
    suspend fun classify(modelPath: String, mboxFileName: String): List<Float> = withContext(Dispatchers.IO) {
        try {
            val python = PythonSingleton.instance
            val pyModule = python.getModule("model_predict")

            // Call the Python function and get the predictions
            val predictions = pyModule.callAttr("predict_on_mbox", modelPath, mboxFileName).asList()

            // Flatten the list of lists and convert each prediction to Float
            val flatPredictions = predictions.flatMap { it.asList() }.map { it.toFloat() }

            Log.d(logTag, "Classification completed successfully")
            flatPredictions
        } catch (e: Exception) {
            Log.e(logTag, "Error during classification: ${e.message}", e)
            emptyList()
        }
    }
}
