package com.martinszuc.phising_emails_detection.data

import android.content.Context

class UserRepository(context: Context) {
    private val sharedPref = context.getSharedPreferences("YourPreferenceName", Context.MODE_PRIVATE)

    fun saveLoginState(isLoggedIn: Boolean) {
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", isLoggedIn)
            apply()
        }
    }

    fun getLoginState(): Boolean {
        return sharedPref.getBoolean("isLoggedIn", false)
    }
}

