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
    val role: Role,
    val age: Int,
    val gender: Int,
    val height: Int,
    val weight: Int,
    val isVerified: Boolean,
    val loginTime: LocalDateTime? = null
)
