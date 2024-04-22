package com.martinszuc.phishing_emails_detection.data.model

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.python.PythonSingleton
import java.io.File
import javax.inject.Inject

private const val logTag = "WeightManagerModule"


/**
 * @author matoszuc@gmail.com
 */

class WeightManager @Inject constructor() {


    fun extractModelWeights(modelName: String): String {
        Log.d(logTag, "Extracting weights for model at: $modelName")

        val python = PythonSingleton.instance
        val pythonScript = python.getModule("utils_weights")

        val weightsFileName = pythonScript.callAttr("serialize_model_weights", modelName).toString()
        Log.d(logTag, "Weights extracted for $modelName")

        return weightsFileName
    }

    fun updateModelWithNewWeights(modelName: String, weightsFile: File) {
        Log.d(logTag, "Updating model at: $modelName with new weights from file: ${weightsFile.path}")

        val python = PythonSingleton.instance
        val pythonScript = python.getModule("utils_weights")

        pythonScript.callAttr("deserialize_and_load_model_weights", modelName, weightsFile.name)
        Log.d(logTag, "Model $modelName updated with new weights from file")
    }
}