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
        pyModule = py?.getModule("model_predict")  // Ensure this matches the name of your Python script file without the '.py' extension
    }

    suspend fun classify(modelPath: String, mboxFilePath: String): List<Float> = withContext(Dispatchers.IO) {
        // Call the Python function and get the predictions
        val predictions = pyModule?.callAttr("predict_on_mbox", modelPath, mboxFilePath)?.asList()

        // Flatten the list of lists and convert each prediction to Float
        val flatPredictions = predictions?.flatMap { it.asList() }?.map { it.toFloat() } ?: emptyList()

        flatPredictions
    }
}
