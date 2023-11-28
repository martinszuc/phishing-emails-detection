package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_parent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailsParentBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_parent.adapter.EmailsPagerAdapter

/**
 * Authored by matoszuc@gmail.com
 */
class EmailsParentFragment : Fragment() {

    private var _binding: FragmentEmailsParentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEmailsParentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager: ViewPager2 = binding.viewPager
        viewPager.adapter = EmailsPagerAdapter(this)

        val tabLayout: TabLayout = binding.tabs
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Imports"
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_email_plus)
                }
                1 -> {
                    tab.text = "Saved"
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_saved)
                }
                2 -> {
                    tab.text = "Detector"
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_email_seal)
                }
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}