package com.martinszuc.phishing_emails_detection.data.local.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
/**
 * This is a UserRepository class that handles user data.
 * It provides methods for getting and saving the user's login state to and from app's shared preferences.
 *
 * @author matoszuc@gmail.com
 */
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
    fun logout() {
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", false)
            apply()
        }
    }
}