package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_parent.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detector.EmailsDetectorFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import.EmailImportFragment
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
            1 -> EmailsDetectorFragment() // Replace with actual fragment
            2 -> EmailImportFragment() // Replace with actual fragment
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}