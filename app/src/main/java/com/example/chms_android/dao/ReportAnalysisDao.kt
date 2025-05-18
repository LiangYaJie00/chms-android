package com.example.chms_android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.chms_android.data.ReportAnalysis

@Dao
interface ReportAnalysisDao {
    /**
     * 插入分析报告
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReportAnalysis(reportAnalysis: ReportAnalysis)
    
    /**
     * 根据ID获取分析报告
     */
    @Query("SELECT * FROM reportAnalysis WHERE id = :id")
    fun getReportAnalysisById(id: Int): ReportAnalysis?
    
    /**
     * 根据用户ID获取所有分析报告
     */
    @Query("SELECT * FROM reportAnalysis WHERE userId = :userId ORDER BY reportDate DESC")
    fun getReportAnalysisByUserId(userId: Int): List<ReportAnalysis>
    
    /**
     * 根据报告ID获取分析报告
     */
    @Query("SELECT * FROM reportAnalysis WHERE reportId = :reportId")
    fun getReportAnalysisByReportId(reportId: Long): ReportAnalysis?
    
    /**
     * 删除分析报告
     */
    @Delete
    fun deleteReportAnalysis(reportAnalysis: ReportAnalysis)
    
    /**
     * 删除所有分析报告
     */
    @Query("DELETE FROM reportAnalysis")
    fun deleteAllReportAnalysis()
}