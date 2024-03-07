package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detector

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.FragmentEmailPackageManagerBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detector.adapter.EmailPackageAdapter
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailPackageSharedViewModel
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.emails.EmailParentSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Authored by matoszuc@gmail.com
 */
@AndroidEntryPoint
class EmailsPackageManagerFragment : Fragment() {
    private var _binding: FragmentEmailPackageManagerBinding? = null
    private val binding get() = _binding!!
    private lateinit var emailPackageAdapter: EmailPackageAdapter
    private val emailPackageManagerViewModel: EmailPackageManagerViewModel by viewModels()
    private val emailPackageSharedViewModel: EmailPackageSharedViewModel by activityViewModels()
    private val emailParentSharedViewModel: EmailParentSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailPackageManagerBinding.inflate(inflater, container, false)
        setupRecyclerView()
        observeViewModel()
        initFloatingActionButton()
        return binding.root
    }

    private fun setupRecyclerView() {
        emailPackageAdapter = EmailPackageAdapter(
            emptyList(),
            onDeleteClicked = { fileName ->
                emailPackageManagerViewModel.deleteEmailPackage(fileName)
                emailPackageSharedViewModel.loadEmailPackages()
            },
            onAddClicked = { view ->
                showAddOptionsPopupMenu(view)
            }
        )
        binding.rvEmailPackages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = emailPackageAdapter
        }
    }

    private fun showAddOptionsPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view, R.style.CustomPopupMenu)
        popup.menuInflater.inflate(R.menu.menu_add_options, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add_from_file -> {
                    // Handle "Add .mbox from File"
                    Log.d("PopupMenu", "Add .mbox from File selected")
                    true
                }

                R.id.action_build_package -> {
                    // Handle "Build Package Within App"
                    Log.d("PopupMenu", "Build Package Within App selected")
                    emailParentSharedViewModel.setViewPagerPosition(0)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    private fun observeViewModel() {
        emailPackageSharedViewModel.emailPackages.observe(viewLifecycleOwner) { packages ->
            Log.d("EmailsPackageManager", "Packages received: ${packages.size}")
            emailPackageAdapter.setItems(packages)
        }
    }

    private fun initFloatingActionButton() {
        val fab: FloatingActionButton = binding.fab
        fab.show()
        fab.setOnClickListener {
            emailPackageSharedViewModel.loadEmailPackages()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
