package com.example.chms_android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chms_android.data.HealthInfo

@Dao
interface HealthInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHealthInfo(healthInfo: HealthInfo)

    @Query("SELECT * FROM healthInfo WHERE userId = :userId LIMIT 1")
    fun getHealthInfoByUserId(userId: Int): HealthInfo?
}