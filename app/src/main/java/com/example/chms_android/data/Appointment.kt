package com.example.chms_android.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "appointment")
data class Appointment(
    @PrimaryKey(autoGenerate = false)
    val appointmentId: Int = 0,
    val patientId: Int? = null,
    val patientName: String? = null,
    val doctorId: Int? = null,
    val doctorName: String? = null,
    val appointmentDate: Date? = null,
    val startTime: Date? = null,
    val endTime: Date? = null,
    val reason: String? = null,
    val appointmentType: AppointmentType = AppointmentType.OFFLINE,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val notificationSent: Boolean = false,
    val notes: String? = null,
    val createTime: Date? = null,
    val updateTime: Date? = null
) : Parcelable {
    @Ignore
    constructor() : this(
        appointmentId = 0
    )
}

enum class AppointmentStatus {
    PENDING,    // 待确认
    CONFIRMED,  // 已确认
    COMPLETED,  // 已完成
    CANCELLED,  // 已取消
    REJECTED    // 已拒绝
}

enum class AppointmentType {
    ONLINE,     // 线上预约
    OFFLINE     // 线下预约
}