package com.example.chms_android.api

import android.content.Context
import com.example.chms_android.data.User
import com.example.chms_android.utils.OkhttpUtil
import com.google.gson.Gson

object UserApi {

    // 用户信息更新
    fun updateUser(user: User, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson = Gson()
        val jsonBody = gson.toJson(user)

        OkhttpUtil.postRequest("/user/updateUser", jsonBody, context, callback, needsToken = true, useJson = true)
    }
}