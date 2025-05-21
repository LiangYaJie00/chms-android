package com.example.chms_android.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.io.Serializable

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = false)
    val userId: Int? = null,
    var name: String,
    val email: String,
    val password: String? = null,
    var phone: Long? = null,
    var avatar: String? = null,
    val role: Role,
    var age: Int? = null,
    var gender: Int? = null,
    var height: Int? = null,
    var weight: Int? = null,
    var community: String? = null,
    val isVerified: Boolean,
    val loginTime: LocalDateTime? = null
) : Serializable
