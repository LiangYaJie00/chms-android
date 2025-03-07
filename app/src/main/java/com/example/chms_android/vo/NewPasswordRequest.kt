package com.example.chms_android.vo

data class NewPasswordRequest(
    val email: String,
    val password: String,
    val code: String
)