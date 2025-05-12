package com.stafftracker.utils

/**
 * API Configuration for Staff Tracker
 * This class centralizes API configuration for the entire app
 */
object ApiConfig {
    // Set this to your deployed backend URL
    const val BASE_URL = "https://appp-bx2c.onrender.com"
    
    // API Endpoints
    const val LOGIN_ENDPOINT = "/api/login"
    const val REPORTS_ENDPOINT = "/api/reports"
    
    // Authentication
    const val AUTH_HEADER = "Authorization"
    const val BEARER_PREFIX = "Bearer "
    
    // Default credentials for testing
    object DefaultCredentials {
        const val ADMIN_USERNAME = "admin"
        const val ADMIN_PASSWORD = "admin123"
        const val USER_USERNAME = "user"
        const val USER_PASSWORD = "user123"
    }
} 