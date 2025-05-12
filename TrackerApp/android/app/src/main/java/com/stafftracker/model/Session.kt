package com.stafftracker.model

import java.util.*

data class Session(
    val id: String = UUID.randomUUID().toString(),
    val staffId: String = "",
    val checkInTime: Date = Date(),
    val checkOutTime: Date? = null,
    val isActive: Boolean = true,
    val checkInLocation: Map<String, Double> = mapOf(),
    val checkOutLocation: Map<String, Double> = mapOf()
) 