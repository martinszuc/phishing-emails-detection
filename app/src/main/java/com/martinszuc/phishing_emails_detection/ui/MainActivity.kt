package com.martinszuc.phishing_emails_detection.ui

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.ActivityMainBinding
import com.martinszuc.phishing_emails_detection.ui.component.login.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

// TODO add bottom navigation

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val userAccountViewModel: UserAccountViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setupBinding()
        setupToolbar()
        setupBottomNav()
        observeLoginState()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Center the title
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        textView.gravity = Gravity.CENTER
        textView.textSize = 20f
        binding.toolbar.addView(textView)

        // Set the title based on the current fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            textView.text = destination.label
        }
    }


    private fun setupBottomNav() {
        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navView.setupWithNavController(navController)
        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.EmailsParentFragment -> navController.navigate(R.id.EmailsParentFragment)
                R.id.TrainingFragment -> navController.navigate(R.id.TrainingFragment)
                R.id.DetectorFragment -> navController.navigate(R.id.DetectorFragment)
                R.id.LearningFragment -> navController.navigate(R.id.LearningFragment)
                R.id.SettingsFragment -> navController.navigate(R.id.SettingsFragment)
            }
            true
        }
    }

    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun observeLoginState() {
        userAccountViewModel.retrieveAccount(this)
        userAccountViewModel.loginState.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                navigate(R.id.EmailsParentFragment)
            } else {
                navigate(R.id.LoginFragment)
            }
        }
    }

    private fun navigate(destinationId: Int) {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.apply {
            popBackStack()
            navigate(destinationId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_logout -> {
                userAccountViewModel.logout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}