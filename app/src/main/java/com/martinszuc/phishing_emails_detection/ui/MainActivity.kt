package com.martinszuc.phishing_emails_detection.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
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
        setupBinding()
        setupToolbar()
        checkLoginState()
    }

    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun checkLoginState() {
        userAccountViewModel.retrieveAccount(this)
        userAccountViewModel.loginState.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                navigateToDashboard()
            } else {
                navigateToLogin()
            }
        }
    }

    private fun navigateToDashboard() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.apply {
            navigate(R.id.DashboardFragment)
        }
    }

    private fun navigateToLogin() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.apply {
            navigate(R.id.LoginFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}