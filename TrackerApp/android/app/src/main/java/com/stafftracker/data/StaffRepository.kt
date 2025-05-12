package com.stafftracker.data

import android.content.Context
import android.util.Log
import com.stafftracker.model.Session
import com.stafftracker.model.Staff
import com.stafftracker.utils.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream

/**
 * Repository for staff operations
 */
class StaffRepository(private val context: Context) {
    
    private val apiService = ApiService.create()
    private var authToken: String? = null
    
    /**
     * Login to backend
     */
    suspend fun login(username: String, password: String): Result<UserResponse> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                authToken = "${ApiConfig.BEARER_PREFIX}${loginResponse.token}"
                Result.success(loginResponse.user)
            } else {
                Result.failure(Exception("Login failed: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Log.e("StaffRepository", "Login error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Download staff report
     */
    suspend fun downloadStaffReport(
        staffId: String,
        period: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Flow<Result<File>> = flow {
        try {
            if (authToken == null) {
                emit(Result.failure(Exception("Not authenticated. Please login first.")))
                return@flow
            }
            
            val response = apiService.getStaffReport(
                staffId = staffId,
                period = period,
                startDate = startDate,
                endDate = endDate,
                authToken = authToken!!
            )
            
            if (response.isSuccessful && response.body() != null) {
                // Save file to app's files directory
                val fileName = "staff_report_${staffId}_${period ?: "custom"}.xlsx"
                val file = File(context.filesDir, fileName)
                
                response.body()!!.use { body ->
                    FileOutputStream(file).use { outputStream ->
                        body.byteStream().copyTo(outputStream)
                    }
                }
                
                emit(Result.success(file))
            } else {
                emit(Result.failure(Exception("Failed to download report: ${response.errorBody()?.string()}")))
            }
        } catch (e: Exception) {
            Log.e("StaffRepository", "Download report error", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Clear authentication token
     */
    fun logout() {
        authToken = null
    }
} 