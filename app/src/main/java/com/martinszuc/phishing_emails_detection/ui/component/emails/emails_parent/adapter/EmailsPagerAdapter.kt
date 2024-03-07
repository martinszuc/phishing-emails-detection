package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_parent.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detector.EmailsPackageManagerFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import.EmailsImportFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.EmailsSavedFragment

/**
 * Authored by matoszuc@gmail.com
 */
class EmailsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 3 // Adjust the number based on your pages
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> EmailsSavedFragment()
            1 -> EmailsImportFragment()
            2 -> EmailsPackageManagerFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}