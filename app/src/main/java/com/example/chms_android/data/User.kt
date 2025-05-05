package com.example.chms_android.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = false)
    val userId: Int? = null,
    val name: String,
    val email: String,
    val password: String? = null,
    val phone: Long,
    var avatar: String,
    val role: Role,
    val age: Int,
    val gender: Int,
    val height: Int,
    val weight: Int,
    var community: String? = null,
    val isVerified: Boolean,
    val loginTime: LocalDateTime? = null
)
