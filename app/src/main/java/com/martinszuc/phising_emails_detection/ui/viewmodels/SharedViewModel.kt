package com.martinszuc.phising_emails_detection.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.martinszuc.phising_emails_detection.data.UserRepository

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