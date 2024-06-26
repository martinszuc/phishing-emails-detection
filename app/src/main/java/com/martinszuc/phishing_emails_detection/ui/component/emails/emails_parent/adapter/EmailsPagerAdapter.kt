package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_parent.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detection.EmailsDetectionFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import.EmailsGmailFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_package_manager.EmailsPackageManagerFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_processed_manager.ProcessedPackageManagerFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.EmailsSavedFragment

/**
 * Authored by matoszuc@gmail.com
 */
class EmailsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 5 // Adjust the number based on your pages
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> EmailsGmailFragment()
            1 -> EmailsSavedFragment()
            2 -> EmailsDetectionFragment()
            3 -> EmailsPackageManagerFragment()
            4 -> ProcessedPackageManagerFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}