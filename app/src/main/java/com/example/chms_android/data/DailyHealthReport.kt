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
@Entity(tableName = "dailyHealthReports")
data class DailyHealthReport(
    @PrimaryKey(autoGenerate = false)
    val reportId: Int = 0, // 报告的唯一标识符
    val userId: Int = 0, // 与该报告相关联的用户标识符
    val reportDate: Date? = null, // 报告记录的日期
    val heartRate: Int? = null, // 用户的心率，通常以每分钟的心跳次数为单位
    val bloodPressureSystolic: Int? = null, // 用户的收缩压（心脏跳动时动脉中的压力）
    val bloodPressureDiastolic: Int? = null, // 用户的舒张压（心脏静止时动脉中的压力）
    val bodyTemperature: BigDecimal? = null, // 用户的体温，通常以摄氏度或华氏度为单位
    val respiratoryRate: Int? = null, // 用户的呼吸频率，通常以每分钟的呼吸次数为单位
    val weight: BigDecimal? = null, // 用户的体重，通常以千克或磅为单位
    val bmi: BigDecimal? = null, // 用户的身体质量指数（BMI），根据体重和身高计算
    val bloodSugar: BigDecimal? = null, // 用户的血糖水平，通常以 mg/dL 为单位
    val steps: Int? = null, // 用户每天行走的步数
    val exerciseDuration: Int? = null, // 用户进行锻炼的时间，通常以分钟为单位
    val sleepDuration: Int? = null, // 用户的睡眠时长，通常以小时为单位
    val sleepQuality: String? = null, // 睡眠质量，可以是描述性的术语或等级
    val caloriesIntake: Int? = null, // 用户每天摄入的总热量
    val waterIntake: Int? = null, // 用户摄入的水量，通常以升或盎司为单位
    val emotionalState: String? = null, // 用户的情绪状态描述，例如：快乐、悲伤、压力大
    val stressLevel: String? = null, // 用户的压力水平，可以用质量描述或数值评估
    val arterialBloodOxygenLevel: BigDecimal? = null, // 动脉血氧水平，通常以百分比表示
    val venousBloodOxygenLevel: BigDecimal? = null, // 静脉血氧水平，也通常以百分比表示
    val notes: String? = null, // 与报告相关的附加备注或观察
    val createdAt: Timestamp? = null, // 报告创建的时间戳
    val updatedAt: Timestamp? = null, // 报告最后更新的时间戳
): Parcelable {
    @Ignore
    constructor() : this(
        reportId = 0
    )
}
