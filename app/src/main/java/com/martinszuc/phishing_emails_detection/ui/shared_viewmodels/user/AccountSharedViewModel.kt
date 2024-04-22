package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.user

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.martinszuc.phishing_emails_detection.data.data_repository.auth.AccountManager
import com.martinszuc.phishing_emails_detection.data.data_repository.auth.AuthenticationRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val logTag = "AccountSharedViewModel"

/**
 * Authored by matoszuc@gmail.com
 */

@HiltViewModel
class AccountSharedViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val accountManager: AccountManager
) : AbstractBaseViewModel() {
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
        launchDataLoad(
            execution = {
                authenticationRepository.signOut {
                    // On logout, clear the account information and update login state
                    accountManager.saveLoginState(false)
                    accountManager.refreshAccount() // Refresh account to ensure it reflects the logout
                }
            },
            onSuccess = {
                // Optionally handle any success case, such as logging or updating a state variable
                Log.d(logTag, "Mbox files successfully cleared.")
            },
            onFailure = { e ->
                // Handle failure, e.g., log error or update a UI component
                Log.e(logTag, "Error during logout and clear files: ${e.message}")
            })

    }

    fun refreshAccount() {
        accountManager.refreshAccount()
    }

    fun getSignInIntent(): Intent {
        return authenticationRepository.getSignInIntent()
    }
}
