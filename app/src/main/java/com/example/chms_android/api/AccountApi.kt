package com.example.chms_android.api

import android.content.Context
import com.example.chms_android.utils.OkhttpUtil
import com.example.chms_android.vo.LoginRequest
import com.example.chms_android.vo.NewPasswordRequest
import com.example.chms_android.vo.RegisterRequest
import com.example.chms_android.vo.SendCodeRequest
import com.google.gson.Gson

object AccountApi {

    // 登录方法
    fun login(email: String, password: String, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val loginRequest = LoginRequest(email, password)
        val gson = Gson()
        val jsonBody = gson.toJson(loginRequest)

        OkhttpUtil.postRequest("/user/auth/login", jsonBody, context, callback, needsToken = false, useJson = false)
    }

    // 注册方法
    fun register(registerRequest: RegisterRequest, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson = Gson()
        val jsonBody = gson.toJson(registerRequest)

        OkhttpUtil.postRequest("/user/auth/register", jsonBody, context, callback, needsToken = false, useJson = true)
    }

    // 发送验证码
    fun sendCode(email: String, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val sendCodeRequest = SendCodeRequest(email)
        val gson = Gson()
        val jsonBody = gson.toJson(sendCodeRequest)

        OkhttpUtil.postRequest("/user/auth/sendCode", jsonBody, context, callback, needsToken = false, useJson = false)
    }

    // 更新密码
    fun updatePassword(email: String, password: String, code: String, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val newPasswordRequest = NewPasswordRequest(email, password, code)
        val gson = Gson()
        val jsonBody = gson.toJson(newPasswordRequest)

        OkhttpUtil.postRequest("/user/auth/updatePassword", jsonBody, context, callback, needsToken = false, useJson = false)
    }

}