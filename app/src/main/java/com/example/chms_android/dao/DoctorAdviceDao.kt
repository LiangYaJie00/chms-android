package com.example.chms_android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.chms_android.data.DoctorAdvice

@Dao
interface DoctorAdviceDao {
    /**
     * 插入单个医生建议
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDoctorAdvice(doctorAdvice: DoctorAdvice)
    
    /**
     * 插入医生建议列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDoctorAdvices(doctorAdvices: List<DoctorAdvice>)
    
    /**
     * 根据ID获取医生建议
     */
    @Query("SELECT * FROM doctorAdvice WHERE adviceId = :adviceId")
    fun getDoctorAdviceById(adviceId: Int): DoctorAdvice?
    
    /**
     * 获取医生发出的所有建议
     */
    @Query("SELECT * FROM doctorAdvice WHERE doctorId = :doctorId ORDER BY createdAt DESC")
    fun getDoctorAdvicesByDoctorId(doctorId: Int): List<DoctorAdvice>
    
    /**
     * 获取居民收到的所有建议
     */
    @Query("SELECT * FROM doctorAdvice WHERE residentId = :residentId ORDER BY createdAt DESC")
    fun getDoctorAdvicesByResidentId(residentId: Int): List<DoctorAdvice>
    
    /**
     * 获取居民收到的未读建议
     */
    @Query("SELECT * FROM doctorAdvice WHERE residentId = :residentId AND status = 0 ORDER BY createdAt DESC")
    fun getUnreadDoctorAdvicesByResidentId(residentId: Int): List<DoctorAdvice>
    
    /**
     * 获取居民收到的重要建议
     */
    @Query("SELECT * FROM doctorAdvice WHERE residentId = :residentId AND isImportant = 1 ORDER BY createdAt DESC")
    fun getImportantDoctorAdvicesByResidentId(residentId: Int): List<DoctorAdvice>
    
    /**
     * 删除医生建议
     */
    @Delete
    fun deleteDoctorAdvice(doctorAdvice: DoctorAdvice)
}