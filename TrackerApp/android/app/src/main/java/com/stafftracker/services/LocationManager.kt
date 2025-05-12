package com.stafftracker.services

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.stafftracker.model.Session
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.TimeUnit

class LocationManager(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    // Start location tracking for a staff member
    fun startTracking(staffId: String, sessionId: String) {
        // Start foreground service for immediate tracking
        val serviceIntent = Intent(context, LocationTrackingService::class.java).apply {
            putExtra(LocationTrackingService.EXTRA_STAFF_ID, staffId)
            putExtra(LocationTrackingService.EXTRA_SESSION_ID, sessionId)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        
        // Schedule periodic work for background tracking
        schedulePeriodicTracking(staffId, sessionId)
    }
    
    // Stop location tracking
    fun stopTracking() {
        // Stop foreground service
        context.stopService(Intent(context, LocationTrackingService::class.java))
        
        // Cancel periodic work
        workManager.cancelUniqueWork(WORK_NAME)
    }
    
    // Schedule periodic location tracking using WorkManager
    private fun schedulePeriodicTracking(staffId: String, sessionId: String) {
        val inputData = Data.Builder()
            .putString(LocationTrackingWorker.KEY_STAFF_ID, staffId)
            .putString(LocationTrackingWorker.KEY_SESSION_ID, sessionId)
            .build()
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<LocationTrackingWorker>(
            TRACKING_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    // Get current location once
    suspend fun getCurrentLocation(): Location? {
        return try {
            val cancellationToken = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).await()
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing location permission: ${e.message}")
            null
        }
    }
    
    companion object {
        private const val TAG = "LocationManager"
        private const val WORK_NAME = "location_tracking_work"
        private const val TRACKING_INTERVAL_HOURS = 1L
    }
} 