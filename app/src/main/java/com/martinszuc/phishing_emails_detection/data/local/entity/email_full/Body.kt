package com.martinszuc.phishing_emails_detection.data.local.entity.email_full

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Authored by matoszuc@gmail.com
 */
@Parcelize
data class Body(
    val data: String,
    val size: Int
) : Parcelable