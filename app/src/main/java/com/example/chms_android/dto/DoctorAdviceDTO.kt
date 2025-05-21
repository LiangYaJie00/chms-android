package com.example.chms_android.dto

import com.example.chms_android.common.enums.AdviceStatus
import com.example.chms_android.common.enums.AdviceType
import com.example.chms_android.data.DoctorAdvice
import java.sql.Timestamp

data class DoctorAdviceDTO(
    val adviceId: Int = 0,
    val doctorId: Int? = null,
    val doctorName: String? = null,
    val residentId: Int? = null,
    val residentName: String? = null,
    val title: String? = null,
    val content: String? = null,
    val adviceType: Int? = null,
    val adviceTypeName: String? = null,
    val status: Int? = null,
    val statusName: String? = null,
    val isImportant: Int? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    // 将DTO转换为实体类
    fun toEntity(): DoctorAdvice {
        return DoctorAdvice(
            adviceId = adviceId,
            doctorId = doctorId,
            residentId = residentId,
            title = title,
            content = content,
            adviceType = adviceType,
            status = status,
            isImportant = isImportant,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    // 获取建议类型枚举
    fun getAdviceTypeEnum(): AdviceType? {
        return adviceType?.let { AdviceType.getByCode(it) }
    }
    
    // 获取建议状态枚举
    fun getStatusEnum(): AdviceStatus? {
        return status?.let { AdviceStatus.getByCode(it) }
    }
    
    // 判断建议是否重要
    fun isImportant(): Boolean {
        return isImportant == 1
    }
    
    // 判断建议是否已读
    fun isRead(): Boolean {
        return status == AdviceStatus.READ.code
    }
}
