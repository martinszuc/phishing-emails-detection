package com.martinszuc.phishing_emails_detection.ui.component.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.martinszuc.phishing_emails_detection.data.remote.UserManager
import com.martinszuc.phishing_emails_detection.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserAccountViewModel @Inject constructor(
    private val userManager: UserManager
) : BaseViewModel() {
    val account: LiveData<GoogleSignInAccount> get() = userManager.account
    val loginState: LiveData<Boolean> get() = userManager.isUserLoggedIn

    fun saveAccount(account: GoogleSignInAccount) {
        handleException {
            Log.d("UserAccountViewModel", "Handling sign-in result: $account")
            userManager.saveAccount(account)
        }
    }


    fun retrieveAccount(context: Context) {
        Log.d("UserAccountViewModel", "Retrieving account")
        userManager.checkUserSignInState(context)
    }
}
