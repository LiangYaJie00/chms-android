package com.example.chms_android.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.Date

@Parcelize
@Entity(tableName = "healthInfo")
data class HealthInfo(
    @PrimaryKey(autoGenerate = false)
    var healthId: Int, // 健康信息表的主键
    var userId: Int, // 外键，关联到用户表中的用户ID
    var weight: BigDecimal? = null, // 体重，保留两位小数
    var height: BigDecimal? = null, // 身高，保留两位小数
    var bmi: BigDecimal? = null, // BMI指数
    var bloodPressureSystolic: Int? = null, // 收缩压
    var bloodPressureDiastolic: Int? = null, // 舒张压
    var lastMedicalCheckup: Date? = null, // 最近体检日期
    var allergies: String? = null, // 过敏史
    var medicalHistory: String? = null, // 既往病史
    var familyMedicalHistory: String? = null, // 家族病史
    var currentConditions: String? = null, // 当前疾病
    var medications: String? = null, // 用药情况
    var vaccinationRecords: String? = null, // 疫苗接种记录
    var lifestyleHabits: String? = null, // 生活习惯
    var exerciseFrequency: String? = null, // 锻炼频率
    var dietaryPreferences: String? = null, // 饮食习惯
    var createdAt: Timestamp? = null, // 记录创建时间
    var updatedAt: Timestamp? = null, // 记录更新时间
    var heartRate: Int? = null, // 心率
    var arterialBloodOxygenLevel: BigDecimal? = null, // 动脉血氧量，保留两位小数
    var venousBloodOxygenLevel: BigDecimal? = null // 静脉血氧量，保留两位小数
): Parcelable {
    @Ignore
    // 次构造函数，默认为 healthId 和 userId 提供值
    constructor(
        weight: BigDecimal? = null,
        height: BigDecimal? = null,
        bmi: BigDecimal? = null,
        bloodPressureSystolic: Int? = null,
        bloodPressureDiastolic: Int? = null,
        lastMedicalCheckup: Date? = null,
        allergies: String? = null,
        medicalHistory: String? = null,
        familyMedicalHistory: String? = null,
        currentConditions: String? = null,
        medications: String? = null,
        vaccinationRecords: String? = null,
        lifestyleHabits: String? = null,
        exerciseFrequency: String? = null,
        dietaryPreferences: String? = null,
        createdAt: Timestamp? = null,
        updatedAt: Timestamp? = null,
        heartRate: Int? = null,
        arterialBloodOxygenLevel: BigDecimal? = null,
        venousBloodOxygenLevel: BigDecimal? = null
    ) : this(
        healthId = 0,   // 默认值，可根据需要调整
        userId = 0,     // 默认值，可根据需要调整
        weight = weight,
        height = height,
        bmi = bmi,
        bloodPressureSystolic = bloodPressureSystolic,
        bloodPressureDiastolic = bloodPressureDiastolic,
        lastMedicalCheckup = lastMedicalCheckup,
        allergies = allergies,
        medicalHistory = medicalHistory,
        familyMedicalHistory = familyMedicalHistory,
        currentConditions = currentConditions,
        medications = medications,
        vaccinationRecords = vaccinationRecords,
        lifestyleHabits = lifestyleHabits,
        exerciseFrequency = exerciseFrequency,
        dietaryPreferences = dietaryPreferences,
        createdAt = createdAt,
        updatedAt = updatedAt,
        heartRate = heartRate,
        arterialBloodOxygenLevel = arterialBloodOxygenLevel,
        venousBloodOxygenLevel = venousBloodOxygenLevel
    )

}