package com.example.chms_android.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object ToastUtil {
    private val mainHandler = Handler(Looper.getMainLooper())

    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // 如果当前就是在主线程，直接显示
            Toast.makeText(context, message, duration).show()
        } else {
            // 否则，通过Handler在主线程中显示
            mainHandler.post {
                Toast.makeText(context, message, duration).show()
            }
        }
    }
}