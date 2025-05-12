package com.stafftracker.ui.staff

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.stafftracker.R
import com.stafftracker.databinding.ActivityStaffBinding
import com.stafftracker.services.LocationManager
import com.stafftracker.ui.auth.AuthActivity
import com.stafftracker.utils.PermissionUtils
import java.text.SimpleDateFormat
import java.util.*

class StaffActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityStaffBinding
    private lateinit var viewModel: StaffViewModel
    private lateinit var locationManager: LocationManager
    
    private var currentSessionId: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[StaffViewModel::class.java]
        locationManager = LocationManager(this)
        
        setupObservers()
        setupListeners()
        
        // Load current user data
        val currentUser = viewModel.getCurrentUser()
        if (currentUser != null) {
            viewModel.loadStaffData(currentUser.uid)
        } else {
            // User not logged in, redirect to auth screen
            navigateToAuthScreen()
        }
    }
    
    private fun setupObservers() {
        viewModel.staff.observe(this) { staff ->
            // Update UI with staff data
            binding.tvStaffName.text = staff.name
            binding.tvStaffEmail.text = staff.email
            
            // Load staff photo if available
            if (staff.photoUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(staff.photoUrl)
                    .placeholder(R.drawable.ic_person)
                    .into(binding.ivStaffPhoto)
            }
        }
        
        viewModel.state.observe(this) { state ->
            when (state) {
                is StaffViewModel.StaffState.Loading -> showLoading(true)
                is StaffViewModel.StaffState.Success -> showLoading(false)
                is StaffViewModel.StaffState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> showLoading(false)
            }
        }
        
        viewModel.trackingState.observe(this) { trackingState ->
            when (trackingState) {
                is StaffViewModel.TrackingState.Tracking -> {
                    binding.btnCheckInOut.text = getString(R.string.check_out)
                    binding.tvAttendanceStatus.visibility = View.VISIBLE
                    
                    // Show check-in time if available
                    trackingState.since?.let { checkInTime ->
                        val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(checkInTime)
                        binding.tvAttendanceStatus.text = "You are checked in since $formattedTime"
                    } ?: run {
                        binding.tvAttendanceStatus.text = getString(R.string.tracking_active)
                    }
                }
                is StaffViewModel.TrackingState.NotTracking -> {
                    binding.btnCheckInOut.text = getString(R.string.check_in)
                    binding.tvAttendanceStatus.visibility = View.GONE
                }
            }
        }
    }
    
    private fun setupListeners() {
        binding.btnCheckInOut.setOnClickListener {
            val isCheckedIn = viewModel.trackingState.value is StaffViewModel.TrackingState.Tracking
            
            if (isCheckedIn) {
                // Check-out
                requestLocation { location ->
                    val currentUser = viewModel.getCurrentUser()
                    if (currentUser != null && location != null) {
                        // Stop location tracking service
                        locationManager.stopTracking()
                        
                        // Update check-out status
                        viewModel.checkOut(currentUser.uid, location)
                    }
                }
            } else {
                // Check-in
                checkLocationPermission()
            }
        }
        
        binding.btnLogout.setOnClickListener {
            // Check if user is checked in
            val isCheckedIn = viewModel.trackingState.value is StaffViewModel.TrackingState.Tracking
            
            if (isCheckedIn) {
                // Prompt user to check-out first
                Toast.makeText(
                    this,
                    "Please check-out before logging out",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Logout
                viewModel.logout()
                navigateToAuthScreen()
            }
        }
    }
    
    private fun checkLocationPermission() {
        // Check and request location permission
        if (!PermissionUtils.hasLocationPermissions(this, true)) {
            PermissionUtils.requestLocationPermissions(
                this,
                true,
                onGranted = {
                    // Check if location services are enabled
                    if (PermissionUtils.isLocationEnabled(this)) {
                        performCheckIn()
                    } else {
                        PermissionUtils.showLocationServicesDialog(this)
                    }
                },
                onDenied = {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_required),
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        } else {
            // Already have permissions
            if (PermissionUtils.isLocationEnabled(this)) {
                performCheckIn()
            } else {
                PermissionUtils.showLocationServicesDialog(this)
            }
        }
    }
    
    private fun performCheckIn() {
        requestLocation { location ->
            val currentUser = viewModel.getCurrentUser()
            if (currentUser != null && location != null) {
                // Generate a session ID for this check-in
                currentSessionId = UUID.randomUUID().toString()
                
                // Start location tracking service
                currentSessionId?.let { sessionId ->
                    locationManager.startTracking(currentUser.uid, sessionId)
                }
                
                // Update check-in status
                viewModel.checkIn(currentUser.uid, location)
            }
        }
    }
    
    private fun requestLocation(onLocationReceived: (android.location.Location?) -> Unit) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    onLocationReceived(location)
                }
                .addOnFailureListener { e ->
                    showError("Failed to get location: ${e.message}")
                    onLocationReceived(null)
                }
        } catch (e: SecurityException) {
            showError("Location permission required")
            onLocationReceived(null)
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCheckInOut.isEnabled = !isLoading
        binding.btnLogout.isEnabled = !isLoading
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToAuthScreen() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 