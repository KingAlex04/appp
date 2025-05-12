package com.stafftracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp

class StaffTrackerApp : Application(), Configuration.Provider {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Create notification channel for location tracking service
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.tracking_notification_channel)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(LOCATION_TRACKING_CHANNEL_ID, name, importance)
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
    
    companion object {
        const val LOCATION_TRACKING_CHANNEL_ID = "location_tracking_channel"
        const val LOCATION_TRACKING_NOTIFICATION_ID = 1001
    }
} 