package com.example.chms_android.dao

import com.example.chms_android.data.User
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    fun insertUser(user: User): Long

    @Query("SELECT * FROM user WHERE userId = :userId")
    suspend fun getUser(userId: Int): User
}