/**
 * Author: matoszuc@gmail.com
 *
 * This package contains the PythonSingleton object which manages the Python instance for the application.
 *
 * @package com.martinszuc.phishing_emails_detection.data.python
 */
package com.martinszuc.phishing_emails_detection.data.python

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

/**
 * Singleton object for managing Python instance.
 */
object PythonSingleton {
    @Volatile private var python: Python? = null

    /**
     * Initializes the Python instance.
     *
     * @param context The application context.
     */
    fun initialize(context: Context) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    /**
     * Gets the Python instance. If it's not initialized, it initializes it.
     *
     * @return The Python instance.
     */
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
