package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.full.FullDetailsFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.minimal.MinimalDetailsFragment

class DetailsPagerAdapter(
    fa: FragmentActivity,
    private val emailMinimal: EmailMinimal,
    private val emailFull: EmailFull
) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MinimalDetailsFragment.newInstance(emailMinimal)
            1 -> FullDetailsFragment.newInstance(emailFull)
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}