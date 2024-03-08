package com.martinszuc.phishing_emails_detection.utils.machine_learning

import com.chaquo.python.Python

/**
 * Authored by matoszuc@gmail.com
 */

class MachineLearningUtils {

    fun processMboxToCsv(resourcesDir: String, mboxFilename: String, encoding: String, limit: Int, isPhishy: Boolean) {
        val python = Python.getInstance()
        val pythonScript = python.getModule("process_emails_mbox_to_csv")

        // Convert isPhishy to string "true" or "false" for Python script compatibility
        val isPhishyStr = if (isPhishy) "true" else "false"

        pythonScript.callAttr("process_mbox_to_csv", resourcesDir, mboxFilename, encoding, limit.toString(), isPhishyStr)
    }
}