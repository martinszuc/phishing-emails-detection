package com.martinszuc.phishing_emails_detection.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.snackbar.Snackbar
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.repository.UserRepository
import com.martinszuc.phishing_emails_detection.databinding.ActivityMainBinding
import com.martinszuc.phishing_emails_detection.ui.viewmodel.SharedViewModel
import com.martinszuc.phishing_emails_detection.ui.viewmodel.factory.SharedViewModelFactory

// TODO add bottom navigation


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get an instance of UserRepository
        val userRepository = UserRepository(this)
        // Create SharedViewModel instance carrying account information.
        val factory = SharedViewModelFactory(userRepository)
        sharedViewModel = ViewModelProvider(this, factory)[SharedViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Check if user is already logged in
        if (sharedViewModel.getLoginState()) {
            // Get the last signed-in account
            val account = GoogleSignIn.getLastSignedInAccount(this)
            // If the account is not null, update it in SharedViewModel
            if (account != null) {
                sharedViewModel.setAccount(account)
                // Navigate to DashboardFragment and clear back stack
                navController.apply {
                    popBackStack()
                    navigate(R.id.DashboardFragment)
                }
            }
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