package com.example.chms_android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chms_android.data.Community
import com.example.chms_android.data.Doctor

@Dao
interface CommunityDao {
    // 插入 Doctors 列表
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCommunities(communityList: List<Community>)

    // 获取所有的医生
    @Query("SELECT * FROM community")
    fun getAllCommunity(): List<Community>
}