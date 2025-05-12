package com.stafftracker.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stafftracker.data.FirebaseRepository
import com.stafftracker.model.Staff
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState
    
    // Check if user is already logged in
    fun checkCurrentUser() {
        val currentUser = repository.getCurrentUser()
        
        if (currentUser != null) {
            _authState.value = AuthState.Loading
            
            viewModelScope.launch {
                try {
                    // Get user details from Firestore
                    val staff = repository.getStaffById(currentUser.uid)
                    
                    if (staff != null) {
                        _authState.postValue(AuthState.Success(staff))
                    } else {
                        // User exists in Auth but not in Firestore
                        _authState.postValue(AuthState.Error("User profile not found"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking current user: ${e.message}")
                    _authState.postValue(AuthState.Error(e.message ?: "Unknown error"))
                }
            }
        } else {
            // No user logged in, stay in Idle state
            _authState.value = AuthState.Idle
        }
    }
    
    // Login with email and password
    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                // Authenticate with Firebase
                val user = repository.login(email, password)
                
                // Get user details from Firestore
                val staff = repository.getStaffById(user.uid)
                
                if (staff != null) {
                    _authState.postValue(AuthState.Success(staff))
                } else {
                    // User exists in Auth but not in Firestore
                    _authState.postValue(AuthState.Error("User profile not found"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}")
                _authState.postValue(AuthState.Error(e.message ?: "Login failed"))
            }
        }
    }
    
    // Logout user
    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }
    
    // Auth state sealed class
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val staff: Staff) : AuthState()
        data class Error(val message: String) : AuthState()
    }
    
    companion object {
        private const val TAG = "AuthViewModel"
    }
} 