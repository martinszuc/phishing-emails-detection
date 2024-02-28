package com.martinszuc.phishing_emails_detection.ui

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.auth.AuthenticationRepository
import com.martinszuc.phishing_emails_detection.data.model.Classifier
import com.martinszuc.phishing_emails_detection.databinding.ActivityMainBinding
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.user.AccountSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {                                                      // TODO little bar with status of processes
    @Inject
    lateinit var classifier: Classifier
    @Inject
    lateinit var authenticationRepository: AuthenticationRepository

    private val accountSharedViewModel: AccountSharedViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPermaNightMode()
        setupBinding()
        setupToolbar()
        setupBottomNav()
        setupAuthentication()
        launchPython {
            setupClassifier()
        }
    }

    private fun launchPython(onPythonStarted: () -> Unit) {
        GlobalScope.launch {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this@MainActivity))
                withContext(Dispatchers.Main) {
                    onPythonStarted()
                }
            } else {
                withContext(Dispatchers.Main) {
                    onPythonStarted()
                }
            }
        }
    }


    private fun setupClassifier() {
        // Initialize Classifier in the background
        lifecycleScope.launch {
            classifier.initializePython()
//            classifier.loadModel() // commented out until federated learning implementation
        }
    }

    private fun setupPermaNightMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
        }
        setupToolbarTitle()
    }

    private fun setupToolbarTitle() {
        val textView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            textSize = 20f
        }
        binding.toolbar.addView(textView)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            textView.text = destination.label
            updateUIBasedOnDestination(destination.id)
        }
    }

    private fun updateUIBasedOnDestination(destinationId: Int) {
        if (destinationId == R.id.LoginFragment) {
            binding.bottomNavigation.visibility = View.GONE
            binding.toolbar.menu.clear()
        } else {
            binding.bottomNavigation.visibility = View.VISIBLE
        }
    }

    private fun setupBottomNav() {
        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        navView.setupWithNavController(navController)
        navView.setOnItemSelectedListener { item ->
            // Check if the current destination matches the item clicked
            if (navController.currentDestination?.id != item.itemId) {
                val destinationId = when (item.itemId) {
                    R.id.EmailsParentFragment -> R.id.EmailsParentFragment
                    R.id.TrainingFragment -> R.id.TrainingFragment
                    R.id.DetectorFragment -> R.id.DetectorFragment
                    R.id.LearningFragment -> R.id.LearningFragment
                    R.id.SettingsFragment -> R.id.SettingsFragment
                    else -> return@setOnItemSelectedListener false
                }
                navController.navigate(destinationId)
            }
            true  // Return true to indicate the event was handled
        }
    }


    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupAuthentication() {
        accountSharedViewModel.refreshAccount()
        accountSharedViewModel.loginState.observe(this) { isLoggedIn ->
            val destinationId = if (isLoggedIn) R.id.EmailsParentFragment else R.id.LoginFragment
            navigate(destinationId)
            invalidateOptionsMenu()
        }
    }


    private fun navigate(destinationId: Int) {
        findNavController(R.id.nav_host_fragment_content_main).apply {
            popBackStack()
            navigate(destinationId)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Clear the menu but do not inflate a new one
        menu.clear()
        return true
    }
}
