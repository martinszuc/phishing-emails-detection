package com.martinszuc.phishing_emails_detection.ui.component.settings.learning.webview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.martinszuc.phishing_emails_detection.databinding.ActivityWebViewBinding

/**
 * Authored by matoszuc@gmail.com
 */
class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("WebViewActivity", "onCreate() called")
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val url = intent.getStringExtra("URL_KEY")
        Log.d("WebViewActivity", "URL_KEY: $url")
        binding.webview.loadUrl(url!!)
    }
}
