package com.martinszuc.phishing_emails_detection.data.model

import android.content.Context
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel

/**
 * Classifier class for phishing email detection.
 * This class uses a TensorFlow Lite model for classification and a Python script for text preprocessing.
 * @author matoszuc@gmail.com
 */
class Classifier(private val context: Context) {
    private var tflite: Interpreter? = null  // TensorFlow Lite interpreter
    private var py: Python? = null  // Python instance
    private var pyModule: PyObject? = null  // Python module

    init {
        // Initialize Python instance and load Python module
        py = Python.getInstance()
        pyModule = py?.getModule("classifier")
    }

    // Will be used for federated learning implementation
    fun loadModel() {
        // Load TensorFlow Lite model
        val assetManager = context.assets
        val modelPath = "phishing_model.tflite"
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val fileBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        tflite = Interpreter(fileBuffer)
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
