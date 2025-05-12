package com.stafftracker.data

import com.stafftracker.model.Location
import com.stafftracker.model.Session
import com.stafftracker.model.Staff
import com.stafftracker.utils.ApiConfig
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * API Service interface for Staff Tracker API
 */
interface ApiService {
    
    /**
     * Login to the backend
     */
    @POST(ApiConfig.LOGIN_ENDPOINT)
    suspend fun login(
        @Body credentials: LoginRequest
    ): Response<LoginResponse>
    
    /**
     * Get report for staff (admin only)
     * This will return a file download
     */
    @GET("${ApiConfig.REPORTS_ENDPOINT}/{staffId}")
    @Streaming
    suspend fun getStaffReport(
        @Path("staffId") staffId: String,
        @Query("period") period: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Header(ApiConfig.AUTH_HEADER) authToken: String
    ): Response<ResponseBody>
    
    companion object {
        fun create(): ApiService {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            return Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}

/**
 * Login request payload
 */
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * Login response
 */
data class LoginResponse(
    val message: String,
    val token: String,
    val user: UserResponse
)

/**
 * User data from response
 */
data class UserResponse(
    val id: String,
    val username: String,
    val role: String
) 