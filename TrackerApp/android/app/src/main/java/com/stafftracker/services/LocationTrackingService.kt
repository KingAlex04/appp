package com.stafftracker.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.stafftracker.R
import com.stafftracker.StaffTrackerApp
import com.stafftracker.data.FirebaseRepository
import com.stafftracker.model.StaffLocation
import com.stafftracker.ui.staff.StaffActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class LocationTrackingService : Service() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val repository = FirebaseRepository()
    
    private var staffId: String = ""
    private var sessionId: String = ""
    
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        staffId = intent?.getStringExtra(EXTRA_STAFF_ID) ?: ""
        sessionId = intent?.getStringExtra(EXTRA_SESSION_ID) ?: UUID.randomUUID().toString()
        
        // Show notification for foreground service
        val notification = NotificationCompat.Builder(this, StaffTrackerApp.LOCATION_TRACKING_CHANNEL_ID)
            .setContentTitle(getString(R.string.tracking_notification_title))
            .setContentText(getString(R.string.tracking_notification_text))
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(getPendingIntent())
            .setOngoing(true)
            .build()
        
        startForeground(StaffTrackerApp.LOCATION_TRACKING_NOTIFICATION_ID, notification)
        
        // Start location updates
        startLocationUpdates()
        
        return START_STICKY
    }
    
    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, StaffActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(LOCATION_UPDATE_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
            .build()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    onNewLocation(location)
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Lost location permission: ${e.message}")
        }
    }
    
    private fun onNewLocation(location: Location) {
        Log.d(TAG, "New location: $location")
        
        val staffLocation = StaffLocation(
            id = UUID.randomUUID().toString(),
            staffId = staffId,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            provider = location.provider,
            sessionId = sessionId
        )
        
        serviceScope.launch {
            try {
                repository.saveLocation(staffLocation)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save location: ${e.message}")
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
    
    companion object {
        private const val TAG = "LocationTrackingService"
        const val EXTRA_STAFF_ID = "extra_staff_id"
        const val EXTRA_SESSION_ID = "extra_session_id"
        
        // Location update interval (1 hour in milliseconds)
        private const val LOCATION_UPDATE_INTERVAL = 3600000L
        
        // Fastest update interval (15 minutes - this is the minimum time between updates)
        private const val LOCATION_FASTEST_INTERVAL = 900000L
    }
} 