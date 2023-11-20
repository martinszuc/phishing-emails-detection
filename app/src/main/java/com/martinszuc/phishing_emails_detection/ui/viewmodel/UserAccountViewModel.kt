package com.martinszuc.phishing_emails_detection.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.martinszuc.phishing_emails_detection.data.repository.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class UserAccountViewModel @Inject constructor(
    private val userManager: UserManager
) : ViewModel() {
    val account: LiveData<GoogleSignInAccount> get() = userManager.account
    val loginState: LiveData<Boolean> get() = userManager.isUserLoggedIn
    private val _error = MutableLiveData<String>()

    fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d("UserAccountViewModel", "Handling sign-in result: $account")
            userManager.saveAccount(account)
        } catch (e: ApiException) {
            _error.value = "Sign in failed"
        }
    }

    fun retrieveAccount(context: Context) {
        Log.d("UserAccountViewModel", "Retrieving account")
        userManager.checkUserSignInState(context)
    }
}
