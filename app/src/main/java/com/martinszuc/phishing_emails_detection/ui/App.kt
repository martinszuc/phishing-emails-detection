package com.martinszuc.phishing_emails_detection.ui

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Authored by matoszuc@gmail.com
 */

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Start Python in a background thread
        GlobalScope.launch {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this@App))
            }
        }
    }
}