package com.martinszuc.phishing_emails_detection.data.tensor

import android.content.Context
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.util.Locale

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
        pyModule = py?.getModule("tfidf_vectorizer")
    }

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
    fun classify(emailText: String): Float {
        Log.d("Classifier", "Classifying email with body: ${emailText.take(100)}...")
        val inputVal = preprocess(emailText)
        val outputVal = Array(1) { FloatArray(1) }
        tflite?.run(inputVal, outputVal)
        return outputVal[0][0]
    }

    /**
     * Preprocess an email text.
     * Tokenize the text and convert it to a TF-IDF vector using a Python script.
     * @param emailText The email text to preprocess.
     * @return The TF-IDF vector.
     */
    private fun preprocess(emailText: String): Array<FloatArray> {
        // Basic preprocessing in Kotlin
        val processedText = emailText.lowercase(Locale.getDefault())
            .replace("[^a-zA-Z0-9\\s]".toRegex(), "").split("\\s+".toRegex())

        // Further processing and TF-IDF transformation using Python
        val tfidfVector = pyModule?.callAttr("transform_text", processedText.joinToString(" "))?.asList()?.map { it.toFloat() }?.toFloatArray()
        return arrayOf(tfidfVector ?: FloatArray(0))
    }

}
