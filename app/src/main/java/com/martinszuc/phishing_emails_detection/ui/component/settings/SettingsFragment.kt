package com.martinszuc.phishing_emails_detection.ui.component.settings

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.EmailsSavedViewModel
import com.martinszuc.phishing_emails_detection.ui.component.login.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Authored by matoszuc@gmail.com
 */
@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    private val emailsSavedViewModel: EmailsSavedViewModel by activityViewModels()
    private val userAccountViewModel: UserAccountViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "clear_database" -> {
                emailsSavedViewModel.clearDatabase()
                Toast.makeText(context, "Database cleared", Toast.LENGTH_SHORT).show()

            }
            "logout" -> {
                userAccountViewModel.logout(requireContext())
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}
