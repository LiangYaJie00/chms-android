package com.example.chms_android.vo

import com.example.chms_android.data.User

data class UserResponse(
    val user: User,
    val token: String,
    val errorMessage: String
)