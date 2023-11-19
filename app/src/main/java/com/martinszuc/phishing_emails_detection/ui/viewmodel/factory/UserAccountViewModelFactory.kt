package com.martinszuc.phishing_emails_detection.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.martinszuc.phishing_emails_detection.data.repository.UserRepository
import com.martinszuc.phishing_emails_detection.ui.viewmodel.UserAccountViewModel

class UserAccountViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserAccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserAccountViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}