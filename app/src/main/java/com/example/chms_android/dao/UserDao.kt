package com.example.chms_android.dao

import android.util.Log
import com.example.chms_android.data.User
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.example.chms_android.data.Role

@Dao
interface UserDao {
    @Insert
    fun insertUser(user: User): Long

    /**
     * 批量插入用户数据
     * 当有冲突时（相同的主键），替换已有数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(users: List<User>): List<Long>

    @Update
    fun updateUser(user: User): Int

    @Query("SELECT * FROM user WHERE userId = :userId")
    fun getUser(userId: Int): User

    @Query("SELECT * FROM user WHERE email = :email")
    fun getUserByEmail(email: String): User

    /**
     * 获取所有角色为consumer的用户
     */
    @Query("SELECT * FROM user WHERE role = 'consumer'")
    fun getAllConsumerUsers(): List<User>
    
    /**
     * 获取所有角色为doctor的用户
     */
    @Query("SELECT * FROM user WHERE role = 'doctor'")
    fun getAllDoctorUsers(): List<User>
    
    /**
     * 根据角色获取用户
     */
    @Query("SELECT * FROM user WHERE role = :role")
    fun getUsersByRole(role: Role): List<User>
    
    /**
     * 根据角色和社区获取用户
     */
    @Query("SELECT * FROM user WHERE role = :role AND community = :community")
    fun getUsersByRoleAndCommunity(role: Role, community: String): List<User>
}