package com.example.chms_android.dao

import android.util.Log
import com.example.chms_android.data.User
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    fun insertUser(user: User): Long

    @Update
    fun updateUser(user: User): Int

    @Query("SELECT * FROM user WHERE userId = :userId")
    fun getUser(userId: Int): User

    @Query("SELECT * FROM user WHERE email = :email")
    fun getUserByEmail(email: String): User
}