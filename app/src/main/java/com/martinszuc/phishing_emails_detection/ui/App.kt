package com.martinszuc.phishing_emails_detection.ui

import android.app.Application
import com.martinszuc.phishing_emails_detection.data.python.PythonSingleton
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Authored by matoszuc@gmail.com
 */

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Python in the background thread
        val appContext = applicationContext
        GlobalScope.launch(Dispatchers.IO) {
            PythonSingleton.initialize(appContext)
        }
    }
}