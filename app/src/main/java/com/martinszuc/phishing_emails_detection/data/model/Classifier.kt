package com.martinszuc.phishing_emails_detection.data.model

import android.content.Context
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Classifier class for phishing email detection.
 * @author matoszuc@gmail.com
 */
class Classifier @Inject constructor(private val context: Context) {
    private var py: Python? = null  // Python instance
    private var pyModule: PyObject? = null  // Python module

    suspend fun initializePython() = withContext(Dispatchers.IO) {
        py = Python.getInstance()
        pyModule = py?.getModule("classifier")
    }

    /**
     * Classify an email text.
     * @param emailText The email text to classify.
     * @return The classification result.
     */
    fun classify(mboxString: String): Boolean {
        // Call the predict_email function and get the result
        val prediction = pyModule?.callAttr("predict_email", mboxString).toString()

        // Assuming the Python function returns "Phishing" or "Safe"
        return prediction == "Phishing"
    }
}
