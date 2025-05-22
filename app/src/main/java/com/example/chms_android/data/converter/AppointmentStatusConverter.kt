package com.example.chms_android.data.converter

import androidx.room.TypeConverter
import com.example.chms_android.data.AppointmentStatus

class AppointmentStatusConverter {
    @TypeConverter
    fun fromAppointmentStatus(status: AppointmentStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toAppointmentStatus(statusName: String?): AppointmentStatus? {
        return statusName?.let { AppointmentStatus.valueOf(it) }
    }
}