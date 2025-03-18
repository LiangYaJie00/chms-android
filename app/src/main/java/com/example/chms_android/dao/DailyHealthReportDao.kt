package com.example.chms_android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.chms_android.data.DailyHealthReport

@Dao
interface DailyHealthReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDailyHealthReport(dailyHealthReport: DailyHealthReport)
}