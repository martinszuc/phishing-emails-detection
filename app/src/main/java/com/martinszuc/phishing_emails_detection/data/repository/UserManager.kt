package com.martinszuc.phishing_emails_detection.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    private val userRepository: UserRepository
) {
    private val _account = MutableLiveData<GoogleSignInAccount>()
    val account: LiveData<GoogleSignInAccount> get() = _account
    private val _isUserLoggedIn = MutableLiveData<Boolean>()
    val isUserLoggedIn: LiveData<Boolean> get() = _isUserLoggedIn

    init {
        _isUserLoggedIn.value = userRepository.getLoginState()
    }

    fun saveAccount(account: GoogleSignInAccount) {
        Log.d("UserManager", "Saving account: $account")
        _account.value = account
        saveLoginState(true)
    }

    fun checkUserSignInState(context: Context) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        Log.d("UserManager", "Checking user sign-in state: $account")
        if (account != null) {
            saveAccount(account)
        } else {
            saveLoginState(false)
        }
    }

    private fun saveLoginState(isLoggedIn: Boolean) {
        Log.d("UserManager", "Saving login state: $isLoggedIn")
        userRepository.saveLoginState(isLoggedIn)
        _isUserLoggedIn.value = isLoggedIn
    }
}
