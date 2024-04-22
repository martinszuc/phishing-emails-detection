package com.martinszuc.phishing_emails_detection.data.model

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.python.PythonSingleton
import javax.inject.Inject

private const val logTag = "DataProcessingModule"

/**
 * Conversion of mbox email files to CSV format.
 * This class acts as a bridge between the Android application and Python backend processes, using the
 * PythonSingleton to execute Python scripts that perform the data transformation tasks.
 *
 * @author matoszuc@gmail.com
 */
class DataProcessing @Inject constructor() {

    /**
     * Processes an mbox file to CSV format using a specified Python script.
     *
     * @param resourcesDir The directory where the mbox file resides.
     * @param mboxFilename The name of the mbox file to be processed.
     * @param output_dir The directory where the output CSV file will be saved.
     * @param encoding The character encoding used in the mbox file.
     * @param limit The maximum number of emails to process from the mbox file.
     * @param isPhishy Boolean flag indicating whether the emails are considered phishing.
     */
    fun processMboxToCsv(resourcesDir: String, mboxFilename: String, output_dir: String, encoding: String, limit: Int, isPhishy: Boolean) {
        Log.d(logTag, "Starting processMboxToCsv")

        val python = PythonSingleton.instance
        Log.d(logTag, "Python instance obtained")

        val pythonScript = python.getModule("process_emails_mbox_to_csv")
        Log.d(logTag, "Python script module loaded")

        pythonScript.callAttr("process_mbox_to_csv", resourcesDir, mboxFilename, output_dir, encoding, limit, isPhishy)
        Log.d(logTag, "Called process_mbox_to_csv with parameters - resourcesDir: $resourcesDir, mboxFilename: $mboxFilename, encoding: $encoding, limit: $limit, isPhishy: $isPhishy")
    }
}