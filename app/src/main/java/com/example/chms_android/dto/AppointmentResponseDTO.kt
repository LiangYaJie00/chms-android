package com.example.chms_android.dto

import com.example.chms_android.data.AppointmentStatus

data class AppointmentResponseDTO(
    val appointmentId: Int = 0,
    val doctorId: Int? = null,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val notes: String? = null
)