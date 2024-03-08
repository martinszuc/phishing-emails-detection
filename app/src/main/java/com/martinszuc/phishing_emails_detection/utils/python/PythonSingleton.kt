package com.martinszuc.phishing_emails_detection.utils.python

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

object PythonSingleton {
    @Volatile private var python: Python? = null

    fun initialize(context: Context) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    val instance: Python
        get() {
            if (python == null) {
                synchronized(this) {
                    if (python == null) {
                        python = Python.getInstance()
                    }
                }
            }
            return python!!
        }
}