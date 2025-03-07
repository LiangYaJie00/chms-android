package com.example.chms_android.vo

import User

data class UserResponse(
    val user: User,
    val token: String,
    val errorMessage: String
)