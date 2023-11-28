package com.martinszuc.phishing_emails_detection.ui

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.ActivityMainBinding
import com.martinszuc.phishing_emails_detection.ui.component.login.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val userAccountViewModel: UserAccountViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private var isLoggedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setupBinding()
        setupToolbar()
        setupBottomNav()
        observeLoginState()
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
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
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
            if (isLoggedIn) {
                binding.toolbar.inflateMenu(R.menu.menu_main)
            }
        }
    }

    private fun setupBottomNav() {
        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navView.setupWithNavController(navController)
        navView.setOnItemSelectedListener { item ->
            val destinationId = when (item.itemId) {
                R.id.EmailsParentFragment -> R.id.EmailsParentFragment
                R.id.TrainingFragment -> R.id.TrainingFragment
                R.id.DetectorFragment -> R.id.DetectorFragment
                R.id.LearningFragment -> R.id.LearningFragment
                R.id.SettingsFragment -> R.id.SettingsFragment
                else -> return@setOnItemSelectedListener false
            }
            navController.navigate(destinationId)
            true
        }
    }

    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun observeLoginState() {
        userAccountViewModel.retrieveAccount(this)
        userAccountViewModel.loginState.observe(this) { loggedIn ->
            isLoggedIn = loggedIn
            val destinationId = if (loggedIn) R.id.EmailsParentFragment else R.id.LoginFragment
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
        menu.clear()
        if (isLoggedIn) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                userAccountViewModel.logout(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
