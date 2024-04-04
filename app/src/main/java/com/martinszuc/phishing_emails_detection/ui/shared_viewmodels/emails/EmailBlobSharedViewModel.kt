package com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails

import com.martinszuc.phishing_emails_detection.data.email.local.repository.EmailBlobLocalRepository
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */
@HiltViewModel
class EmailBlobSharedViewModel @Inject constructor(
    private val emailBlobLocalRepository: EmailBlobLocalRepository
) : AbstractBaseViewModel() {


}