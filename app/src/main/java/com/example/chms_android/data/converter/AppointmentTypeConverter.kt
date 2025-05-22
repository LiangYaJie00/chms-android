package com.example.chms_android.data.converter

import androidx.room.TypeConverter
import com.example.chms_android.data.AppointmentType

class AppointmentTypeConverter {
    @TypeConverter
    fun fromAppointmentType(type: AppointmentType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toAppointmentType(typeName: String?): AppointmentType? {
        return typeName?.let { AppointmentType.valueOf(it) }
    }
}