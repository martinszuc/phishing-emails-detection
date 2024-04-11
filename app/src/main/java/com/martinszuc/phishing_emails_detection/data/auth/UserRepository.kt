package com.martinszuc.phishing_emails_detection.data.auth

import android.content.Context
import com.martinszuc.phishing_emails_detection.utils.StringUtils
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
        private const val USER_ID_KEY = "userId"
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

    /**
     * Retrieves the user's unique ID.
     *
     * If a unique ID doesn't exist, it generates a new one, saves it, and then returns it.
     *
     * @return [String] representing the user's unique ID.
     */
    fun getUserId(): String {
        // Check if the user ID already exists
        var userId = sharedPref.getString(USER_ID_KEY, null)
        if (userId.isNullOrEmpty()) {
            // If not, generate a new one
            userId = StringUtils.generateClientId()
            saveUserId(userId)
        }
        return userId
    }

    /**
     * Saves the user's unique ID in SharedPreferences.
     *
     * @param userId [String] representing the user's unique ID to be saved.
     */
    private fun saveUserId(userId: String) {
        sharedPref.edit().putString(USER_ID_KEY, userId).apply()
    }
}