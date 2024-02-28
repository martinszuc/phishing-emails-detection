package com.martinszuc.phishing_emails_detection.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
/**
 * Manages user authentication state persistence.
 *
 * Provides functionality to get and save the user's login state in the application's shared preferences.
 * This class encapsulates all shared preferences operations related to the user authentication state,
 * abstracting the details of shared preferences usage away from the rest of the application.
 *
 * @property context The application context used to access [SharedPreferences].
 * @author matoszuc@gmail.com
 */
class UserRepository @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private const val PREF_FILE_NAME = "AuthPreference"
        private const val IS_LOGGED_IN_KEY = "isLoggedIn"
    }

    private val sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    /**
     * Retrieves the user's current login state.
     *
     * @return [Boolean] indicating whether the user is logged in (`true`) or not (`false`).
     */
    fun getLoginState(): Boolean = sharedPref.getBoolean(IS_LOGGED_IN_KEY, false)

    /**
     * Saves the user's login state.
     *
     * @param isLoggedIn [Boolean] indicating the user's login state to be saved.
     */
    fun saveLoginState(isLoggedIn: Boolean) {
        sharedPref.edit().putBoolean(IS_LOGGED_IN_KEY, isLoggedIn).apply()
    }
}