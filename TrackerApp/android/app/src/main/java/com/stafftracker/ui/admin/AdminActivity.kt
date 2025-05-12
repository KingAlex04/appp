package com.stafftracker.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.stafftracker.R
import com.stafftracker.databinding.ActivityAdminBinding
import com.stafftracker.ui.auth.AuthActivity

class AdminActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAdminBinding
    private lateinit var viewModel: AdminViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]
        
        setupToolbar()
        setupNavigation()
        
        // Check if current user is an admin
        viewModel.checkCurrentUser { isAdmin ->
            if (!isAdmin) {
                // Not an admin, redirect to auth screen
                navigateToAuthScreen()
            }
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Setup the bottom navigation with the navigation controller
        binding.bottomNavigationView.setupWithNavController(navController)
        
        // Setup the action bar with the navigation controller
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment,
                R.id.staffListFragment,
                R.id.reportsFragment,
                R.id.settingsFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Handle navigation item selection
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard -> navController.navigate(R.id.dashboardFragment)
                R.id.staff -> navController.navigate(R.id.staffListFragment)
                R.id.reports -> navController.navigate(R.id.reportsFragment)
                R.id.settings -> navController.navigate(R.id.settingsFragment)
            }
            true
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
    
    private fun navigateToAuthScreen() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 