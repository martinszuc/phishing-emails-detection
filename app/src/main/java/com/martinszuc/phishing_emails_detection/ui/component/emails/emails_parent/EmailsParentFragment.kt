package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_parent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailsParentBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_parent.adapter.EmailsPagerAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailParentSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class EmailsParentFragment : Fragment() {

    private var _binding: FragmentEmailsParentBinding? = null
    private val emailParentSharedViewModel: EmailParentSharedViewModel by activityViewModels()

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailsParentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        observePagerPosition()
    }

    private fun observePagerPosition() {
        emailParentSharedViewModel.viewPagerPosition.observe(viewLifecycleOwner) { position ->
            binding.viewPager.currentItem = position
        }
    }


    private fun setupViewPager() {
        val viewPager: ViewPager2 = binding.viewPager
        viewPager.adapter = EmailsPagerAdapter(this)

        val tabLayout: TabLayout = binding.tabs
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Import"
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_email_plus)
                }

                1 -> {
                    tab.text = "Saved"
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_saved)
                }

                2 -> {
                    tab.text = "Packaged"
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_box_package)
                }
                3 -> {
                    tab.text = "Processed"
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_package_processed)
                }
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}