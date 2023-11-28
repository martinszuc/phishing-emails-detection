package com.martinszuc.phishing_emails_detection.data.remote

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.martinszuc.phishing_emails_detection.data.local.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
/**
 * This is a UserManager class that handles user authentication.
 * It provides methods for saving the user's account information and checking the user's sign-in state.
 * It interacts with UserRepository to get and save the login state.
 *
 * @property userRepository The UserRepository instance for interacting with local user data.
 * @property _account The current user's GoogleSignInAccount.
 * @property account A LiveData of the current user's GoogleSignInAccount.
 * @property _isUserLoggedIn The current user's login state.
 * @property isUserLoggedIn A LiveData of the current user's login state.
 *
 * @author matoszuc@gmail.com
 */
@Singleton
class UserManager @Inject constructor(
    private val userRepository: UserRepository
) {
    private val _account = MutableLiveData<GoogleSignInAccount?>()
    val account: LiveData<GoogleSignInAccount?> get() = _account
    private val _isUserLoggedIn = MutableLiveData<Boolean>()
    val isUserLoggedIn: LiveData<Boolean> get() = _isUserLoggedIn

    init {
        _isUserLoggedIn.value = userRepository.getLoginState()
    }

    fun saveAccount(account: GoogleSignInAccount) {
        Log.d("UserManager", "Saving account: $account")
        _account.value = account
        saveLoginState(true)
    }

    fun checkUserSignInState(context: Context) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        Log.d("UserManager", "Checking user sign-in state: $account")
        if (account != null) {
            saveAccount(account)
        } else {
            saveLoginState(false)
        }
    }

    fun logout(context: Context) {
        Log.d("UserManager", "Logging out")

        // Get a GoogleSignInClient instance
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)

        // Sign out the currently signed in user
        googleSignInClient.signOut().addOnCompleteListener {
            _account.value = null
            userRepository.logout()
            _isUserLoggedIn.value = false
        }
    }


    private fun saveLoginState(isLoggedIn: Boolean) {
        Log.d("UserManager", "Saving login state: $isLoggedIn")
        userRepository.saveLoginState(isLoggedIn)
        _isUserLoggedIn.value = isLoggedIn
    }
}
