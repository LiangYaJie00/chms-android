package com.example.chms_android.dto

import com.example.chms_android.data.DoctorAvailableTime
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DoctorAvailableTimeDTO(
    val timeId: Int = 0,
    val doctorId: Int? = null,
    val doctorName: String? = null,
    val availableDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val isBooked: Boolean = false,
    val createTime: Timestamp? = null,
    val updateTime: Timestamp? = null
) {
    // 将DTO转换为实体类
    fun toEntity(): DoctorAvailableTime {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        return DoctorAvailableTime(
            timeId = timeId,
            doctorId = doctorId,
            availableDate = availableDate?.let { dateFormat.parse(it) },
            startTime = startTime?.let { timeFormat.parse(it) },
            endTime = endTime?.let { timeFormat.parse(it) },
            isBooked = isBooked,
            createTime = createTime,
            updateTime = updateTime
        )
    }
}