package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_parent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailsParentBinding
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_parent.adapter.EmailsPagerAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailParentSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class EmailsParentFragment : AbstractBaseFragment() {

    private var _binding: FragmentEmailsParentBinding? = null
    private val emailParentSharedViewModel: EmailParentSharedViewModel by activityViewModels()

    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private var emailsPagerAdapter: EmailsPagerAdapter? = null
    private var tabLayoutMediator: TabLayoutMediator? = null

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

        viewPager = binding.viewPager
        tabLayout = binding.tabs

        setupViewPager()
        observePagerPosition()
    }

    private fun observePagerPosition() {
        emailParentSharedViewModel.viewPagerPosition.observe(viewLifecycleOwner) { position ->
            viewPager.currentItem = position
        }
    }

    private fun setupViewPager() {
        emailsPagerAdapter = EmailsPagerAdapter(this)
        viewPager.adapter = emailsPagerAdapter

        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Gmail"
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_gmail_icon)
                }

                1 -> {
                    tab.text = "Saved"
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_saved)
                }

                2 -> {
                    tab.text = "Labeled"
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_email_protected)
                }

                3 -> {
                    tab.text = "Packaged"
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_box_package)
                }
                4 -> {
                    tab.text = "Processed"
                    tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_package_processed)
                }
            }
        }
        tabLayoutMediator?.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Detach TabLayoutMediator to prevent memory leaks
        tabLayoutMediator?.detach()
        tabLayoutMediator = null

        _binding = null
    }
}
