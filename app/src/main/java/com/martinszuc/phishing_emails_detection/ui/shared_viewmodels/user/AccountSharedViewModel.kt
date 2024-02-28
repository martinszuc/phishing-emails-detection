package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.user

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.martinszuc.phishing_emails_detection.data.auth.AccountManager
import com.martinszuc.phishing_emails_detection.data.auth.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountSharedViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val accountManager: AccountManager
) : ViewModel() {
    val loginState: LiveData<Boolean> = accountManager.isUserLoggedIn
    val account: LiveData<GoogleSignInAccount?> = accountManager.googleAccount

    fun signInWithGoogle(data: Intent?) {
        authenticationRepository.handleSignInResult(data, onSuccess = {
            accountManager.saveLoginState(true)
            accountManager.refreshAccount()
        }, onFailure = {
            accountManager.saveLoginState(false)
//            accountManager.refreshAccount()
        })
    }

    fun logout() {
        authenticationRepository.signOut {
            // On logout, clear the account information and update login state
            accountManager.saveLoginState(false)
            accountManager.refreshAccount() // Refresh account to ensure it reflects the logout
        }
    }

    fun refreshAccount() {
        accountManager.refreshAccount()
    }

    fun getSignInIntent(): Intent {
        return authenticationRepository.getSignInIntent()
    }
}
