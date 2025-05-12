package com.stafftracker.ui.admin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stafftracker.data.FirebaseRepository
import com.stafftracker.model.Staff
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _activeStaff = MutableLiveData<List<Staff>>()
    val activeStaff: LiveData<List<Staff>> = _activeStaff
    
    private val _allStaff = MutableLiveData<List<Staff>>()
    val allStaff: LiveData<List<Staff>> = _allStaff
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // Check if current user is an admin
    fun checkCurrentUser(callback: (Boolean) -> Unit) {
        val currentUser = repository.getCurrentUser()
        
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val staff = repository.getStaffById(currentUser.uid)
                    
                    if (staff != null) {
                        callback(staff.role == Staff.ROLE_ADMIN)
                    } else {
                        callback(false)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking current user: ${e.message}")
                    callback(false)
                }
            }
        } else {
            callback(false)
        }
    }
    
    // Load all staff
    fun loadAllStaff() {
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val staff = repository.getAllStaff()
                _allStaff.postValue(staff)
                _loading.postValue(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading staff: ${e.message}")
                _error.postValue(e.message)
                _loading.postValue(false)
            }
        }
    }
    
    // Load active staff
    fun loadActiveStaff() {
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val staff = repository.getActiveStaff()
                _activeStaff.postValue(staff)
                _loading.postValue(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading active staff: ${e.message}")
                _error.postValue(e.message)
                _loading.postValue(false)
            }
        }
    }
    
    // Create/update staff
    fun saveStaff(staff: Staff, password: String? = null) {
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                if (staff.id.isEmpty() && password != null) {
                    // Create new staff
                    repository.registerStaff(staff.email, password, staff)
                } else {
                    // Update existing staff
                    repository.updateStaff(staff)
                }
                _loading.postValue(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving staff: ${e.message}")
                _error.postValue(e.message)
                _loading.postValue(false)
            }
        }
    }
    
    // Logout admin
    fun logout() {
        repository.logout()
    }
    
    companion object {
        private const val TAG = "AdminViewModel"
    }
} 