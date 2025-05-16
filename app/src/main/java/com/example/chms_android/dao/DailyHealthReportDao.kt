package com.example.chms_android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.chms_android.data.DailyHealthReport

@Dao
interface DailyHealthReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDailyHealthReport(dailyHealthReport: DailyHealthReport)
    
    @Query("SELECT * FROM dailyHealthReports WHERE reportId = :reportId")
    fun getDailyHealthReportById(reportId: Int): DailyHealthReport?
    
    @Query("SELECT * FROM dailyHealthReports WHERE userId = :userId ORDER BY reportDate DESC")
    fun getDailyHealthReportsByUserId(userId: Int): List<DailyHealthReport>
    
    @Query("SELECT * FROM dailyHealthReports WHERE userId = :userId AND reportDate = :reportDate")
    fun getDailyHealthReportByUserIdAndDate(userId: Int, reportDate: String): DailyHealthReport?
    
    @Delete
    fun deleteDailyHealthReport(dailyHealthReport: DailyHealthReport)
    
    @Query("DELETE FROM dailyHealthReports")
    fun deleteAllDailyHealthReports()
}
