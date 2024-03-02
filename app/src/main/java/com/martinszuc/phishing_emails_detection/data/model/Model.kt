package com.martinszuc.phishing_emails_detection.data.model

import android.content.Context
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Model @Inject constructor(private val context: Context) {
    private var py: Python? = null
    private var pyModule: PyObject? = null

    suspend fun initializePython() = withContext(Dispatchers.IO) {
        py = Python.getInstance()
        pyModule = py?.getModule("model")  // Ensure this matches the name of your Python script
    }

    // Update this method to accept a file path instead of mboxString
    suspend fun classify(mboxFilePath: String): Boolean = withContext(Dispatchers.IO) {
        // Call the predict_email function with the file path
        val predictionResult = pyModule?.callAttr("predict_email", mboxFilePath)?.toString() ?: "Error"
        predictionResult == "Phishing"
    }

    // Optionally, create a method for training if you plan to train the model directly from the app.
    // Note: Training a model on a mobile device might not be feasible for larger datasets or more complex models.
}
