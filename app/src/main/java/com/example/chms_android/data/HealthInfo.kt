package com.example.chms_android.data

import java.math.BigDecimal
import java.util.Date

data class HealthInfo(
    var healthId: Int? = null, // 健康信息表的主键
    var userId: Int? = null, // 外键，关联到用户表中的用户ID
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
    var createdAt: Date? = null, // 记录创建时间
    var updatedAt: Date? = null, // 记录更新时间
    var heartRate: Int? = null, // 心率
    var arterialBloodOxygenLevel: BigDecimal? = null, // 动脉血氧量，保留两位小数
    var venousBloodOxygenLevel: BigDecimal? = null // 静脉血氧量，保留两位小数
)