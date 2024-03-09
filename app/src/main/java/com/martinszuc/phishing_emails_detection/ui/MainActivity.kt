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
import com.martinszuc.phishing_emails_detection.data.model.Prediction
import com.martinszuc.phishing_emails_detection.databinding.ActivityMainBinding
import com.martinszuc.phishing_emails_detection.ui.shared_viewmodels.user.AccountSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

/**
 * Authored by matoszuc@gmail.com
 */

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {                                                      // TODO little bar with status of processes
    @Inject
    lateinit var prediction: Prediction

    private val accountSharedViewModel: AccountSharedViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPermaNightMode()
        setupBinding()
        setupToolbar()
        setupBottomNav()
        setupAuthentication()

        setupBackgroundTasks()

        launchPython {
            setupClassifier()
        }
    }

    private fun setupBackgroundTasks() {
        // Copy CSV files to internal storage in a background thread
        lifecycleScope.launch(Dispatchers.IO) {
            copyCsvFilesToInternalStorage()
        }
    }

    private fun copyCsvFilesToInternalStorage() {
        try {
            val assetsFolder = "email_csv_samples"
            val filesToCopy = assets.list(assetsFolder) ?: arrayOf() // List all files in the assets folder

            val internalStorageFolder = File(filesDir, assetsFolder)
            if (!internalStorageFolder.exists()) {
                internalStorageFolder.mkdir() // Create the folder if it doesn't exist
            }

            filesToCopy.forEach { filename ->
                val fileInInternalStorage = File(internalStorageFolder, filename)
                if (!fileInInternalStorage.exists()) {
                    // Copy each file from assets to internal storage
                    val inputStream = assets.open("$assetsFolder/$filename")
                    val outputStream = FileOutputStream(fileInInternalStorage)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.flush()
                    outputStream.close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
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
            prediction.initializePython()
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
