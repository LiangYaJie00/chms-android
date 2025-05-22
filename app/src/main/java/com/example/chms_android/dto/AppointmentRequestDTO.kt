package com.example.chms_android.dto

import com.example.chms_android.data.AppointmentType

data class AppointmentRequestDTO(
    val patientId: Int? = null,
    val doctorId: Int? = null,
    val appointmentDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val reason: String? = null,
    val appointmentType: AppointmentType = AppointmentType.OFFLINE
)