package com.stafftracker.services

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.stafftracker.data.FirebaseRepository
import com.stafftracker.model.StaffLocation
import kotlinx.coroutines.tasks.await
import java.util.UUID

class LocationTrackingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val repository = FirebaseRepository()
    
    override suspend fun doWork(): Result {
        val staffId = inputData.getString(KEY_STAFF_ID) ?: return Result.failure()
        val sessionId = inputData.getString(KEY_SESSION_ID) ?: return Result.failure()
        
        try {
            // Get current location
            val location = getCurrentLocation()
            if (location != null) {
                // Save location to Firebase
                val staffLocation = StaffLocation(
                    id = UUID.randomUUID().toString(),
                    staffId = staffId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    provider = location.provider,
                    sessionId = sessionId
                )
                
                repository.saveLocation(staffLocation)
                return Result.success()
            }
            
            return Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking location: ${e.message}")
            return Result.retry()
        }
    }
    
    private suspend fun getCurrentLocation(): Location? {
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
        private const val TAG = "LocationTrackingWorker"
        const val KEY_STAFF_ID = "key_staff_id" 
        const val KEY_SESSION_ID = "key_session_id"
    }
} 