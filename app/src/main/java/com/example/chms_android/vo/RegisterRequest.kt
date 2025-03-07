package com.example.chms_android.vo

import com.example.chms_android.data.Role

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val phone: Long,
    val code: String,
    val role: Role
)