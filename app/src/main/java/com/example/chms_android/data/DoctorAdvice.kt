package com.example.chms_android.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.chms_android.common.enums.AdviceStatus
import com.example.chms_android.common.enums.AdviceType
import com.example.chms_android.dto.DoctorAdviceDTO
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import java.sql.Timestamp

@Parcelize
@Entity(tableName = "doctorAdvice")
data class DoctorAdvice(
    @PrimaryKey(autoGenerate = false)
    var adviceId: Int = 0,
    var doctorId: Int? = null,
    var residentId: Int? = null,
    var title: String? = null,
    var content: String? = null,
    var adviceType: Int? = null,
    var status: Int? = null,
    var isImportant: Int? = null,
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null
): Parcelable {
    // 将实体类转换为DTO
    fun toDTO(): DoctorAdviceDTO {
        return DoctorAdviceDTO(
            adviceId = adviceId,
            doctorId = doctorId,
            residentId = residentId,
            title = title,
            content = content,
            adviceType = adviceType,
            adviceTypeName = adviceType?.let { AdviceType.getNameByCode(it) },
            status = status,
            statusName = status?.let { AdviceStatus.getNameByCode(it) },
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
    fun getIsImportantFlag(): Boolean {
        return isImportant == 1
    }
    
    // 判断建议是否已读
    fun isRead(): Boolean {
        return status == AdviceStatus.READ.code
    }
    
    // 标记为已读
    fun markAsRead() {
        status = AdviceStatus.READ.code
    }
    
    // 标记为重要
    fun markAsImportant(important: Boolean = true) {
        isImportant = if (important) 1 else 0
    }
    
    @Ignore
    constructor() : this(
        adviceId = 0
    )
}