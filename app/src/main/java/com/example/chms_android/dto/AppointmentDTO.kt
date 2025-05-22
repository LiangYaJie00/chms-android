package com.example.chms_android.dto

import com.example.chms_android.data.Appointment
import com.example.chms_android.data.AppointmentStatus
import com.example.chms_android.data.AppointmentType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppointmentDTO(
    val appointmentId: Int = 0,
    val patientId: Int? = null,
    val patientName: String? = null,
    val doctorId: Int? = null,
    val doctorName: String? = null,
    val appointmentDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val reason: String? = null,
    val appointmentType: AppointmentType = AppointmentType.OFFLINE,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val notificationSent: Boolean = false,
    val notes: String? = null,
    val createTime: Date? = null,
    val updateTime: Date? = null
) {
    // 将DTO转换为实体类
    fun toEntity(): Appointment {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        return Appointment(
            appointmentId = appointmentId,
            patientId = patientId,
            patientName = patientName,
            doctorId = doctorId,
            doctorName = doctorName,
            appointmentDate = appointmentDate?.let { dateFormat.parse(it) },
            startTime = startTime?.let { timeFormat.parse(it) },
            endTime = endTime?.let { timeFormat.parse(it) },
            reason = reason,
            appointmentType = appointmentType,
            status = status,
            notificationSent = notificationSent,
            notes = notes,
            createTime = createTime,
            updateTime = updateTime
        )
    }
    
    companion object {
        // 将实体类转换为DTO
        fun fromEntity(appointment: Appointment): AppointmentDTO {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            return AppointmentDTO(
                appointmentId = appointment.appointmentId,
                patientId = appointment.patientId,
                patientName = appointment.patientName,
                doctorId = appointment.doctorId,
                doctorName = appointment.doctorName,
                appointmentDate = appointment.appointmentDate?.let { dateFormat.format(it) },
                startTime = appointment.startTime?.let { timeFormat.format(it) },
                endTime = appointment.endTime?.let { timeFormat.format(it) },
                reason = appointment.reason,
                appointmentType = appointment.appointmentType,
                status = appointment.status,
                notificationSent = appointment.notificationSent,
                notes = appointment.notes,
                createTime = appointment.createTime,
                updateTime = appointment.updateTime
            )
        }
    }
}