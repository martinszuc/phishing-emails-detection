package com.martinszuc.phishing_emails_detection.data.data_repository.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
/**
 * Manages authentication processes including Google Sign-In and Firebase authentication.
 *
 * This repository abstracts the authentication logic from the rest of the application,
 * providing a clean interface for initiating sign-in processes, handling sign-in results,
 * signing out, and retrieving the current signed-in Google account.
 *
 * @property context The application context used for various operations, such as retrieving string resources.
 * @property firebaseAuth The FirebaseAuth instance used for signing in with Google credentials to Firebase.
 * @property userRepository The repository used for persisting the user's login state.
 * @constructor Creates an instance of AuthenticationRepository responsible for managing authentication.
 * @author matoszuc@gmail.com
 */
@Singleton
class AuthenticationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) {
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.login_web_client_id))
            .requestEmail()
            .requestScopes(Scope(Constants.GMAIL_READONLY_SCOPE))
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    /**
     * Provides the sign-in [Intent] from the GoogleSignInClient for starting the sign-in process.
     *
     * @return The sign-in [Intent] for initiating the Google sign-in flow.
     */
    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    /**
     * Handles the result from the Google Sign-In flow.
     *
     * Attempts to authenticate with Firebase using the Google Sign-In account's ID token.
     * Calls onSuccess if the authentication is successful, onFailure otherwise.
     *
     * @param data The [Intent] data returned from the Google Sign-In flow.
     * @param onSuccess A callback invoked when authentication is successful, providing the [GoogleSignInAccount].
     * @param onFailure A callback invoked when authentication fails.
     */
    fun handleSignInResult(data: Intent?, onSuccess: (GoogleSignInAccount) -> Unit, onFailure: () -> Unit) {
        GoogleSignIn.getSignedInAccountFromIntent(data).run {
            try {
                val account = getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!, onSuccess, onFailure)
            } catch (e: ApiException) {
                onFailure()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, onSuccess: (GoogleSignInAccount) -> Unit, onFailure: () -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val account = GoogleSignIn.getLastSignedInAccount(context)
                account?.let {
                    userRepository.saveLoginState(true)
                    onSuccess(it)
                }
            } else {
                onFailure()
            }
        }
    }

    /**
     * Signs the user out from Firebase and Google.
     *
     * After signing out, it updates the user's login state to false.
     * @param onComplete A callback invoked after the sign-out process is complete.
     */
    fun signOut(onComplete: () -> Unit) {
        firebaseAuth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            userRepository.saveLoginState(false)
            onComplete()
        }
    }

    /**
     * Retrieves the currently signed-in Google account, if any.
     *
     * @return The [GoogleSignInAccount] if the user is currently signed in, null otherwise.
     */
    fun getCurrentAccount(): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
}
