package com.martinszuc.phishing_emails_detection.data.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user authentication state at a higher level than [UserRepository].
 *
 * This class provides LiveData observables for the UI to observe the user's login state,
 * making it easy to react to changes in the user's authentication status.
 * It serves as a bridge between the UI and data layer for authentication state.
 *
 * @property userRepository The [UserRepository] instance used for accessing the user's login state.
 * @constructor Creates an instance of [AccountManager] that manages the user's login state.
 * @author matoszuc@gmail.com
 */
@Singleton
class AccountManager @Inject constructor(
    private val userRepository: UserRepository,
    private val authenticationRepository: AuthenticationRepository
) {
    private val _isUserLoggedIn = MutableLiveData<Boolean>()
    val isUserLoggedIn: LiveData<Boolean> = _isUserLoggedIn

    private val _googleAccount = MutableLiveData<GoogleSignInAccount?>()
    val googleAccount: LiveData<GoogleSignInAccount?> = _googleAccount

    fun saveLoginState(isLoggedIn: Boolean) {
        userRepository.saveLoginState(isLoggedIn)
        refreshAccount()
    }

    fun refreshAccount() {
        val isLoggedIn = userRepository.getLoginState()
        _isUserLoggedIn.value = isLoggedIn

        if (isLoggedIn) {
            // If the user is logged in, refresh the Google account information
            _googleAccount.value = authenticationRepository.getCurrentAccount()
        } else {
            // If the user is not logged in, set the Google account LiveData to null
            _googleAccount.value = null
        }
    }
}
