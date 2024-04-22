package com.martinszuc.phishing_emails_detection.data.data_repository.remote.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Manages the base URL for network requests.
 *
 * Provides functionality to get and save the base URL in the application's shared preferences.
 * This class encapsulates all shared preferences operations related to network base URL,
 * abstracting the details of shared preferences usage away from the rest of the application.
 *
 * @property context The application context used to access [SharedPreferences].
 *
 * @author matoszuc@gmail.com
 */
class BaseUrlRepository @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private const val PREF_FILE_NAME = "NetworkPreference"
        private const val BASE_URL_KEY = "baseUrl"
        private const val DEFAULT_URL = "https://192.168.50.115:5000/"
    }

    private val sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    /**
     * Retrieves the base URL.
     *
     * @return [String] containing the current base URL.
     */
    fun getBaseUrl(): String = sharedPref.getString(BASE_URL_KEY, DEFAULT_URL) ?: DEFAULT_URL

    /**
     * Saves the base URL.
     *
     * @param baseUrl [String] containing the new base URL to be saved.
     */
    fun saveBaseUrl(baseUrl: String) {
        sharedPref.edit().putString(BASE_URL_KEY, baseUrl).apply()
    }
}
