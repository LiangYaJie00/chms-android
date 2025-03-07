package com.example.chms_android.vo

data class RespResult<T>(
    val code: String,
    val message: String,
    val data: T
)