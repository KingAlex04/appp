package com.stafftracker.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.permissionx.guolindev.PermissionX
import com.stafftracker.R
import androidx.fragment.app.FragmentActivity

object PermissionUtils {
    
    // Request location permissions
    fun requestLocationPermissions(
        activity: FragmentActivity,
        requireBackgroundLocation: Boolean = false,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        // Add background location permission if needed and if on Android 10+
        if (requireBackgroundLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        
        PermissionX.init(activity)
            .permissions(permissionsToRequest)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "Location permission is needed for staff tracking",
                    "OK",
                    "Cancel"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "You need to allow location permissions in Settings manually",
                    "Settings",
                    "Cancel"
                )
            }
            .request { allGranted, _, _ ->
                if (allGranted) {
                    onGranted()
                } else {
                    onDenied()
                }
            }
    }
    
    // Check if all location permissions are granted
    fun hasLocationPermissions(context: Context, requireBackgroundLocation: Boolean = false): Boolean {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        // Check for background location permission if required and if on Android 10+
        val hasBackgroundLocation = if (requireBackgroundLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Background location not required or not applicable on this Android version
        }
        
        return hasFineLocation && hasCoarseLocation && hasBackgroundLocation
    }
    
    // Check if location services are enabled
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
    // Show dialog to enable location services
    fun showLocationServicesDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Location Services Required")
            .setMessage("Please enable location services to use check-in functionality")
            .setPositiveButton("Settings") { _, _ ->
                // Open location settings
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
} 