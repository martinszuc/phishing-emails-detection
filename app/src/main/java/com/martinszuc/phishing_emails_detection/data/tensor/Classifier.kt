package com.martinszuc.phishing_emails_detection.data.tensor

/**
 * Authored by matoszuc@gmail.com
 */
import android.app.Activity
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.StringReader
import java.nio.channels.FileChannel

class Classifier(activity: Activity) {
    private var tflite: Interpreter? = null
    private var py: Python? = null
    private var pyModule: PyObject? = null

    init {
        val assetManager = activity.assets
        val modelPath = "assets/phishing_model.tflite" // replace with your model's path
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val fileBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        tflite = Interpreter(fileBuffer)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(activity))
        }
        py = Python.getInstance()
        pyModule = py?.getModule("tfid_vectorizer")
    }

    fun classify(emailText: String): Float {
        val inputVal = preprocess(emailText) // replace with your preprocessing method
        val outputVal = Array(1) { FloatArray(1) }
        tflite?.run(inputVal, outputVal)

        return outputVal[0][0] // return the classification result
    }

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

        // Call Python function to convert tokens to TF-IDF vector
        val tfidfVector = pyModule?.callAttr("transform_text", tokens.joinToString(" "))?.asList()?.map { it.toFloat() }?.toFloatArray()
        return arrayOf(tfidfVector ?: FloatArray(0))
    }
}
