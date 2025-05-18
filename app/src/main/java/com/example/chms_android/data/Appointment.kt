package com.example.chms_android.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Appointment(
    val id: String,
    val patientId: String,
    val patientName: String,
    val doctorId: String,
    val doctorName: String,
    val date: Date,
    val startTime: String,
    val endTime: String,
    val reason: String,
    val status: AppointmentStatus,
    val notes: String = ""
) : Parcelable

enum class AppointmentStatus {
    PENDING,    // 待确认
    CONFIRMED,  // 已确认
    COMPLETED,  // 已完成
    CANCELLED   // 已取消
}