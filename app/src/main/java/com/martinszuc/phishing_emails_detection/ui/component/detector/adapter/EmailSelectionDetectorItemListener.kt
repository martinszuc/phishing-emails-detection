package com.martinszuc.phishing_emails_detection.ui.component.detector.adapter
/**
 * This method is triggered when an email item in the list is clicked.
 * Implementing this method allows the handling of click events on email items.
 *
 * Usage:
 * This method should be implemented by the fragment or activity where the email items are displayed.
 * When an email item is clicked, this method is called with the emailId of the clicked item.
 * It can be used to perform actions such as navigating to a detailed view of the email or marking the email as selected.
 *
 * @param emailId The ID of the clicked email, used to identify which email was selected.
 *
 * @author matoszuc@gmail.com
 */
interface EmailSelectionDetectorItemListener {
    fun onEmailClicked(emailId: String)
}
