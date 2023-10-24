package com.martinszuc.phising_emails_detection.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class SharedViewModel : ViewModel() {
    val account = MutableLiveData<GoogleSignInAccount>()
}