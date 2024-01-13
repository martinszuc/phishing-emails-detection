package com.martinszuc.phishing_emails_detection.data.tensor

import android.app.Activity
import android.content.Context
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.StringReader
import java.nio.channels.FileChannel

/**
 * Classifier class for phishing email detection.
 * This class uses a TensorFlow Lite model for classification and a Python script for text preprocessing.
 * @author matoszuc@gmail.com
 */
class Classifier(context: Context) {
    private var tflite: Interpreter? = null  // TensorFlow Lite interpreter
    private var py: Python? = null  // Python instance
    private var pyModule: PyObject? = null  // Python module

    init {
        // Load TensorFlow Lite model
//        val assetManager = context.assets
//        val modelPath = "phishing_model.tflite"
//        val fileDescriptor = assetManager.openFd(modelPath)
//        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
//        val fileChannel = inputStream.channel
//        val startOffset = fileDescriptor.startOffset
//        val declaredLength = fileDescriptor.declaredLength
//        val fileBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
//        tflite = Interpreter(fileBuffer)
    }

    fun initializePython(activity: Activity) {         // Initialize Python instance and load Python module
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(activity))
        }
        py = Python.getInstance()
        pyModule = py?.getModule("tfidf_vectorizer")
    }

    /**
     * Classify an email text.
     * @param emailText The email text to classify.
     * @return The classification result.
     */
    fun classify(emailText: String): Float {

        Log.d("Classifier", "Classifying email with body: ${emailText.take(100)}...")
        Log.d("Classifier", "Wait starts")
        Thread.sleep(5000)
        Log.d("Classifier", "Wait ends")



//        val inputVal = preprocess(emailText)
//        val outputVal = Array(1) { FloatArray(1) }
//        tflite?.run(inputVal, outputVal)
//        return outputVal[0][0]

        return 0.2f
    }

    /**
     * Preprocess an email text.
     * Tokenize the text and convert it to a TF-IDF vector using a Python script.
     * @param emailText The email text to preprocess.
     * @return The TF-IDF vector.
     */
    private fun preprocess(emailText: String): Array<FloatArray> {
        val analyzer = StandardAnalyzer()
        val stream = analyzer.tokenStream(null, StringReader(emailText))
        val term = stream.addAttribute(CharTermAttribute::class.java)

        stream.reset()
        val tokens = mutableListOf<String>()
        while (stream.incrementToken()) {
            tokens.add(term.toString())
        }
        stream.end()
        stream.close()

        val tfidfVector = pyModule?.callAttr("transform_text", tokens.joinToString(" "))?.asList()?.map { it.toFloat() }?.toFloatArray()
        return arrayOf(tfidfVector ?: FloatArray(0))
    }
}
