package com.example.chms_android.dto

data class EducationDTO(
    val school: String? = null,    // 学校名称
    val degree: String? = null,    // 学位
    val major: String? = null,     // 专业
    val start: String? = null,     // 开始时间
    val end: String? = null        // 结束时间
)