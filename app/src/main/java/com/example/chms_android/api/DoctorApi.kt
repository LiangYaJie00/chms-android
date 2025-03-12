package com.example.chms_android.api

import android.content.Context
import com.example.chms_android.utils.OkhttpUtil

object DoctorApi {

    fun getAllDoctors(context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("/doctor/getAll", context, callback, needsToken = false)
    }
}