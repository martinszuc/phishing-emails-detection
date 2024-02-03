package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import androidx.lifecycle.ViewModel
import com.martinszuc.phishing_emails_detection.data.local.repository.EmailBlobLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
@HiltViewModel
class EmailBlobSharedViewModel @Inject constructor(
    private val emailBlobLocalRepository: EmailBlobLocalRepository
) : ViewModel() {


}