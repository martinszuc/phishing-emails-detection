package com.martinszuc.phishing_emails_detection.ui.view

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.snackbar.Snackbar
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.FragmentLoginBinding
import com.martinszuc.phishing_emails_detection.ui.viewmodel.UserAccountViewModel

// TODO should this be a login activity?
// TODO logout and change google account

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var userAccountViewModel: UserAccountViewModel

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        userAccountViewModel =
            ViewModelProvider(requireActivity())[UserAccountViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureSignIn()
        observeViewModel()

    }

    private fun observeViewModel() {
        userAccountViewModel.loginState.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                findNavController().apply {
                    navigate(R.id.action_LoginFragment_to_DashboardFragment)
                }
            }
        }

        userAccountViewModel.error.observe(viewLifecycleOwner) {
            userAccountViewModel.error.observe(viewLifecycleOwner) { error ->
                error?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    userAccountViewModel.setError(error)
                }
            }
        }
    }

    private fun configureSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic profile.
        // and a request for Gmail scope.

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly")) // Request read-only access to Gmail
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.signInButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                userAccountViewModel.handleSignInResult(task)
            }
        }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
