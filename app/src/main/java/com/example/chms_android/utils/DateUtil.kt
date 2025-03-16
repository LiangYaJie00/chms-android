package com.example.chms_android.utils

import java.sql.Timestamp

object DateUtil {
    fun getCurrentTimestamp(): Timestamp {
        val currentTimeMillis = System.currentTimeMillis()
        return Timestamp(currentTimeMillis)
    }
}