package com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Authored by matoszuc@gmail.com
 */
@Parcelize
data class Header(
    val name: String,
    val value: String
) : Parcelable