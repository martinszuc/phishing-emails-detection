package com.martinszuc.phishing_emails_detection.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.martinszuc.phishing_emails_detection.data.repository.UserRepository

class UserAccountViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _account = MutableLiveData<GoogleSignInAccount>()
    val account: LiveData<GoogleSignInAccount> get() = _account

    private val _loginState = MutableLiveData<Boolean>()
    val loginState: LiveData<Boolean> get() = _loginState

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun setAccount(account: GoogleSignInAccount) {
        _account.value = account
    }

    fun setError(error: String) {
        _error.value = error
    }

    fun saveLoginState(isLoggedIn: Boolean) {
        userRepository.saveLoginState(isLoggedIn)
        _loginState.value = isLoggedIn
    }

    fun getLoginState(): Boolean {
        return userRepository.getLoginState()
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            setAccount(account)
            saveLoginState(true)
        } catch (e: ApiException) {
            _error.value = "Sign in failed"
        }
    }
}
