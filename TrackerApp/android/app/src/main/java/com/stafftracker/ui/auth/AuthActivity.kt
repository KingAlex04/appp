package com.stafftracker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.stafftracker.R
import com.stafftracker.databinding.ActivityAuthBinding
import com.stafftracker.model.Staff
import com.stafftracker.ui.admin.AdminActivity
import com.stafftracker.ui.staff.StaffActivity

class AuthActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAuthBinding
    private lateinit var viewModel: AuthViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        setupObservers()
        setupListeners()
        
        // Check if user is already logged in
        viewModel.checkCurrentUser()
    }
    
    private fun setupObservers() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> showLoading(true)
                is AuthViewModel.AuthState.Success -> {
                    showLoading(false)
                    navigateToAppropriateScreen(state.staff)
                }
                is AuthViewModel.AuthState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is AuthViewModel.AuthState.Idle -> showLoading(false)
            }
        }
    }
    
    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true
        
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        
        return isValid
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToAppropriateScreen(staff: Staff) {
        if (staff.role == Staff.ROLE_ADMIN) {
            // Navigate to Admin Dashboard
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        } else {
            // Navigate to Staff Dashboard
            val intent = Intent(this, StaffActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
} 