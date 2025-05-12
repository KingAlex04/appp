package com.stafftracker.data

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.stafftracker.model.Staff
import com.stafftracker.model.StaffLocation
import com.stafftracker.model.Session
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private val staffCollection = firestore.collection("staff")
    private val locationsCollection = firestore.collection("locations")
    private val sessionsCollection = firestore.collection("sessions")
    
    // Auth operations
    suspend fun login(email: String, password: String): FirebaseUser {
        return suspendCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    result.user?.let { user ->
                        continuation.resume(user)
                    } ?: continuation.resumeWithException(Exception("Login failed"))
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
    
    suspend fun registerStaff(email: String, password: String, staff: Staff): Staff {
        // 1. Create authentication account
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("Failed to create user")
        
        // 2. Store staff profile in Firestore
        val staffWithId = staff.copy(id = uid)
        staffCollection.document(uid).set(staffWithId).await()
        
        return staffWithId
    }
    
    fun logout() {
        auth.signOut()
    }
    
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    // Staff operations
    suspend fun getStaffById(staffId: String): Staff? {
        val document = staffCollection.document(staffId).get().await()
        return document.toObject(Staff::class.java)
    }
    
    suspend fun getAllStaff(): List<Staff> {
        val querySnapshot = staffCollection
            .whereEqualTo("role", Staff.ROLE_STAFF)
            .get()
            .await()
        
        return querySnapshot.documents.mapNotNull { it.toObject(Staff::class.java) }
    }
    
    suspend fun getActiveStaff(): List<Staff> {
        val querySnapshot = staffCollection
            .whereEqualTo("role", Staff.ROLE_STAFF)
            .whereEqualTo("isActive", true)
            .get()
            .await()
        
        return querySnapshot.documents.mapNotNull { it.toObject(Staff::class.java) }
    }
    
    suspend fun updateStaff(staff: Staff) {
        staffCollection.document(staff.id).set(staff).await()
    }
    
    suspend fun updateStaffActiveStatus(staffId: String, isActive: Boolean, timestamp: Date) {
        val updates = if (isActive) {
            mapOf(
                "isActive" to true,
                "lastCheckInTime" to timestamp
            )
        } else {
            mapOf(
                "isActive" to false,
                "lastCheckOutTime" to timestamp
            )
        }
        
        staffCollection.document(staffId).update(updates).await()
    }
    
    // Upload staff photo
    suspend fun uploadStaffPhoto(staffId: String, photoUri: Uri): String {
        val storageRef = storage.reference.child("staff_photos/$staffId.jpg")
        val uploadTask = storageRef.putFile(photoUri).await()
        return storageRef.downloadUrl.await().toString()
    }
    
    // Location operations
    suspend fun saveLocation(location: StaffLocation) {
        locationsCollection.document(location.id).set(location).await()
    }
    
    suspend fun getStaffLocations(staffId: String, startDate: Date, endDate: Date): List<StaffLocation> {
        val querySnapshot = locationsCollection
            .whereEqualTo("staffId", staffId)
            .whereGreaterThanOrEqualTo("timestamp", startDate)
            .whereLessThanOrEqualTo("timestamp", endDate)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()
        
        return querySnapshot.documents.mapNotNull { it.toObject(StaffLocation::class.java) }
    }
    
    // Session operations
    suspend fun startSession(staffId: String, location: StaffLocation): Session {
        val session = Session(
            staffId = staffId,
            checkInTime = Date(),
            isActive = true,
            checkInLocation = mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude
            )
        )
        
        sessionsCollection.document(session.id).set(session).await()
        return session
    }
    
    suspend fun endSession(sessionId: String, location: StaffLocation) {
        val updates = mapOf(
            "checkOutTime" to Date(),
            "isActive" to false,
            "checkOutLocation" to mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude
            )
        )
        
        sessionsCollection.document(sessionId).update(updates).await()
    }
    
    suspend fun getActiveSessions(): List<Session> {
        val querySnapshot = sessionsCollection
            .whereEqualTo("isActive", true)
            .get()
            .await()
        
        return querySnapshot.documents.mapNotNull { it.toObject(Session::class.java) }
    }
    
    suspend fun getStaffSessions(staffId: String, startDate: Date, endDate: Date): List<Session> {
        val querySnapshot = sessionsCollection
            .whereEqualTo("staffId", staffId)
            .whereGreaterThanOrEqualTo("checkInTime", startDate)
            .whereLessThanOrEqualTo("checkInTime", endDate)
            .get()
            .await()
        
        return querySnapshot.documents.mapNotNull { it.toObject(Session::class.java) }
    }
} 