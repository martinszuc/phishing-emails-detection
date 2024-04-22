package com.martinszuc.phishing_emails_detection.ui.component.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.martinszuc.phishing_emails_detection.databinding.FragmentLoginBinding
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseFragment
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.user.AccountSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class LoginFragment : AbstractBaseFragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val accountSharedViewModel: AccountSharedViewModel by activityViewModels()

    private val signInResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            accountSharedViewModel.signInWithGoogle(result.data)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSignInButton()
    }

    private fun setupSignInButton() {
        binding.signInButton.setOnClickListener {
            val signInIntent = accountSharedViewModel.getSignInIntent()
            signInResultLauncher.launch(signInIntent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
