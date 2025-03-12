package com.example.chms_android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chms_android.data.Doctor

@Dao
interface DoctorDao {
    // 插入 Doctors 列表
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDoctors(doctors: List<Doctor>)

    // 获取所有的医生
    @Query("SELECT * FROM doctor")
    fun getAllDoctors(): List<Doctor>

    // 获取前7条医生记录
    @Query("SELECT * FROM doctor LIMIT 7")
    fun getFirstSevenDoctors(): List<Doctor>
}