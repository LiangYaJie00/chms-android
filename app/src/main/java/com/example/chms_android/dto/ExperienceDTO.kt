package com.example.chms_android.dto

data class ExperienceDTO(
    val hospital: String? = null,  // 医院/机构名称
    val position: String? = null,  // 职位
    val start: String? = null,     // 开始时间
    val end: String? = null        // 结束时间
)