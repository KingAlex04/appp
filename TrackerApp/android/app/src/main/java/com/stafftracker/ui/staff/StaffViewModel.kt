package com.stafftracker.ui.staff

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.stafftracker.data.FirebaseRepository
import com.stafftracker.model.Session
import com.stafftracker.model.Staff
import com.stafftracker.model.StaffLocation
import kotlinx.coroutines.launch
import java.util.Date

class StaffViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _staff = MutableLiveData<Staff>()
    val staff: LiveData<Staff> = _staff
    
    private val _state = MutableLiveData<StaffState>(StaffState.Idle)
    val state: LiveData<StaffState> = _state
    
    private val _trackingState = MutableLiveData<TrackingState>()
    val trackingState: LiveData<TrackingState> = _trackingState
    
    private var currentSession: Session? = null
    
    // Get current logged in user
    fun getCurrentUser(): FirebaseUser? {
        return repository.getCurrentUser()
    }
    
    // Load staff data
    fun loadStaffData(staffId: String) {
        _state.value = StaffState.Loading
        
        viewModelScope.launch {
            try {
                val staffData = repository.getStaffById(staffId)
                if (staffData != null) {
                    _staff.postValue(staffData)
                    _state.postValue(StaffState.Success)
                    
                    // Update tracking state based on staff active status
                    if (staffData.isActive) {
                        _trackingState.postValue(TrackingState.Tracking(staffData.lastCheckInTime))
                    } else {
                        _trackingState.postValue(TrackingState.NotTracking)
                    }
                } else {
                    _state.postValue(StaffState.Error("Staff data not found"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading staff data: ${e.message}")
                _state.postValue(StaffState.Error(e.message ?: "Unknown error"))
            }
        }
    }
    
    // Check-in staff member
    fun checkIn(staffId: String, location: Location) {
        _state.value = StaffState.Loading
        
        viewModelScope.launch {
            try {
                // Create staff location
                val staffLocation = StaffLocation(
                    staffId = staffId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    provider = location.provider
                )
                
                // Save location
                repository.saveLocation(staffLocation)
                
                // Start session
                val session = repository.startSession(staffId, staffLocation)
                currentSession = session
                
                // Update staff status
                repository.updateStaffActiveStatus(staffId, true, Date())
                
                // Update staff object
                loadStaffData(staffId)
                
                // Update tracking state
                _trackingState.postValue(TrackingState.Tracking(Date()))
                _state.postValue(StaffState.Success)
            } catch (e: Exception) {
                Log.e(TAG, "Error during check-in: ${e.message}")
                _state.postValue(StaffState.Error(e.message ?: "Check-in failed"))
            }
        }
    }
    
    // Check-out staff member
    fun checkOut(staffId: String, location: Location) {
        _state.value = StaffState.Loading
        
        viewModelScope.launch {
            try {
                // Create staff location
                val staffLocation = StaffLocation(
                    staffId = staffId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    provider = location.provider,
                    sessionId = currentSession?.id ?: ""
                )
                
                // Save location
                repository.saveLocation(staffLocation)
                
                // End session if we have one
                currentSession?.let { session ->
                    repository.endSession(session.id, staffLocation)
                }
                
                // Update staff status
                repository.updateStaffActiveStatus(staffId, false, Date())
                
                // Update staff object
                loadStaffData(staffId)
                
                // Update tracking state
                _trackingState.postValue(TrackingState.NotTracking)
                _state.postValue(StaffState.Success)
            } catch (e: Exception) {
                Log.e(TAG, "Error during check-out: ${e.message}")
                _state.postValue(StaffState.Error(e.message ?: "Check-out failed"))
            }
        }
    }
    
    // Logout user
    fun logout() {
        repository.logout()
    }
    
    // Staff state sealed class
    sealed class StaffState {
        object Idle : StaffState()
        object Loading : StaffState()
        object Success : StaffState()
        data class Error(val message: String) : StaffState()
    }
    
    // Tracking state sealed class
    sealed class TrackingState {
        object NotTracking : TrackingState()
        data class Tracking(val since: Date?) : TrackingState()
    }
    
    companion object {
        private const val TAG = "StaffViewModel"
    }
} 