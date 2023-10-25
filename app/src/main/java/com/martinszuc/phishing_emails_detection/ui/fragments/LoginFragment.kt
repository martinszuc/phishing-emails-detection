package com.martinszuc.phishing_emails_detection.ui.fragments

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
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.FragmentLoginBinding
import com.martinszuc.phishing_emails_detection.ui.viewmodels.SharedViewModel

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedViewModel: SharedViewModel

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure sign-in to request the user's ID, email address, and basic profile.
        // Add a request for Gmail scope.
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

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Login successful
                // Save logged in state to SharedPreferences
                val account = task.getResult(ApiException::class.java)
                sharedViewModel.account.value = account
                sharedViewModel.saveLoginState(true)

                findNavController().apply {
                    navigate(R.id.action_LoginFragment_to_DashboardFragment)
                }

            } catch (e: ApiException) {
                // TODO Sign in failed, handle the failure scenario
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}