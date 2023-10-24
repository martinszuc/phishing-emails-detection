package com.martinszuc.phishing_emails_detection.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.martinszuc.phishing_emails_detection.data.repository.UserRepository

class SharedViewModel(private val userRepository: UserRepository) : ViewModel() {

    val account = MutableLiveData<GoogleSignInAccount>()

    fun setAccount(account: GoogleSignInAccount) {
        this.account.value = account
    }

    fun saveLoginState(isLoggedIn: Boolean) {
        userRepository.saveLoginState(isLoggedIn)
    }

    fun getLoginState(): Boolean {
        return userRepository.getLoginState()
    }
}