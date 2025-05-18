package com.example.chms_android.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.sql.Timestamp
import java.util.Date

/**
 * 健康报告AI分析结果实体类
 */
@Parcelize
@Entity(tableName = "reportAnalysis")
data class ReportAnalysis(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,             // 分析报告的id
    val userId: Int = 0,         // 对应的用户id
    val reportId: Long = 0,      // 对应的报告id
    val answer: String? = null,  // 分析报告的内容
    val reportDate: Date? = null,  // 报告日期
    val createdAt: Timestamp? = null // 分析报告创建时间
) : Parcelable {
    @Ignore
    constructor() : this(
        id = 0
    )
}