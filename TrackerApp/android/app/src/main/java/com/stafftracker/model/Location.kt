package com.stafftracker.model

import com.google.firebase.firestore.GeoPoint
import java.util.*

data class StaffLocation(
    val id: String = "",
    val staffId: String = "",
    val timestamp: Date = Date(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,
    val provider: String = "",
    val sessionId: String = "" // To group locations by check-in/check-out sessions
) {
    fun toGeoPoint(): GeoPoint {
        return GeoPoint(latitude, longitude)
    }
    
    companion object {
        fun fromGeoPoint(
            staffId: String,
            geoPoint: GeoPoint,
            accuracy: Float,
            provider: String,
            sessionId: String
        ): StaffLocation {
            return StaffLocation(
                id = UUID.randomUUID().toString(),
                staffId = staffId,
                timestamp = Date(),
                latitude = geoPoint.latitude,
                longitude = geoPoint.longitude,
                accuracy = accuracy,
                provider = provider,
                sessionId = sessionId
            )
        }
    }
} 