package com.example.chms_android.dto

/**
 * 医生可用时间段请求DTO
 */
data class DoctorAvailableTimeRequestDTO(
    // 医生ID
    val doctorId: Int? = null,
    
    // 可用日期（格式：yyyy-MM-dd）
    val availableDate: String? = null,
    
    // 时间段列表
    val timeSlots: List<TimeSlot>? = null
) {
    /**
     * 时间段
     */
    data class TimeSlot(
        // 开始时间（格式：HH:mm）
        val startTime: String? = null,
        
        // 结束时间（格式：HH:mm）
        val endTime: String? = null
    )
}