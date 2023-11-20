package com.martinszuc.phishing_emails_detection.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UserRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val sharedPref = context.getSharedPreferences("YourPreferenceName", Context.MODE_PRIVATE)

    fun getLoginState(): Boolean {
        return sharedPref.getBoolean("isLoggedIn", false)
    }

    fun saveLoginState(isLoggedIn: Boolean) {
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", isLoggedIn)
            apply()
        }
    }
}