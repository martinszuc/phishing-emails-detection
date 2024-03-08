package com.martinszuc.phishing_emails_detection.utils.machine_learning

import android.util.Log
import com.chaquo.python.Python
import com.martinszuc.phishing_emails_detection.utils.python.PythonSingleton
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

class MachineLearningUtils @Inject constructor() {

    private val TAG = "MachineLearningUtils"

    fun processMboxToCsv(resourcesDir: String, mboxFilename: String, output_dir: String, encoding: String, limit: Int, isPhishy: Boolean) {
        Log.d(TAG, "Starting processMboxToCsv")

        val python = PythonSingleton.instance
        Log.d(TAG, "Python instance obtained")

        val pythonScript = python.getModule("process_emails_mbox_to_csv")
        Log.d(TAG, "Python script module loaded")

        pythonScript.callAttr("process_mbox_to_csv", resourcesDir, mboxFilename, output_dir, encoding, limit, isPhishy)
        Log.d(TAG, "Called process_mbox_to_csv with parameters - resourcesDir: $resourcesDir, mboxFilename: $mboxFilename, encoding: $encoding, limit: $limit, isPhishy: $isPhishy")
    }
}