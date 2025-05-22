package com.example.chms_android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chms_android.data.DoctorAvailableTime
import java.util.Date

@Dao
interface DoctorAvailableTimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAvailableTime(availableTime: DoctorAvailableTime): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAvailableTimes(availableTimes: List<DoctorAvailableTime>)

    @Update
    fun updateAvailableTime(availableTime: DoctorAvailableTime)

    @Query("SELECT * FROM doctorAvailableTime WHERE doctorId = :doctorId")
    fun getAvailableTimesByDoctorId(doctorId: Int): List<DoctorAvailableTime>

    @Query("SELECT * FROM doctorAvailableTime WHERE doctorId = :doctorId AND availableDate = :date AND isBooked = 0")
    fun getAvailableTimesByDoctorIdAndDate(doctorId: Int, date: Date): List<DoctorAvailableTime>

    @Query("SELECT * FROM doctorAvailableTime WHERE timeId = :timeId")
    fun getAvailableTimeById(timeId: Int): DoctorAvailableTime?

    @Query("UPDATE doctorAvailableTime SET isBooked = :isBooked WHERE timeId = :timeId")
    fun updateBookingStatus(timeId: Int, isBooked: Boolean)
}