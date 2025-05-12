package com.stafftracker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Staff(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val contactNumber: String = "",
    val bloodGroup: String = "",
    val address: String = "",
    val isActive: Boolean = false,
    val lastCheckInTime: Date? = null,
    val lastCheckOutTime: Date? = null,
    val role: String = "staff"
) : Parcelable {
    
    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_STAFF = "staff"
    }
} 