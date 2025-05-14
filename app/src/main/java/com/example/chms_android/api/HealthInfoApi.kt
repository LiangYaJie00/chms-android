package com.example.chms_android.api

import android.content.Context
import com.example.chms_android.common.Constants
import com.example.chms_android.data.HealthInfo
import com.example.chms_android.utils.AccountUtil
import com.example.chms_android.utils.OkhttpUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.sql.Timestamp
import java.text.SimpleDateFormat

object HealthInfoApi {
    // 添加健康信息
    fun addHealthInfo(healthInfo: HealthInfo, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // 设置全局日期格式
            .registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
                src?.let {
                    JsonPrimitive(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(src))
                }
            })
            .create()
        val jsonBody = gson.toJson(healthInfo)

        OkhttpUtil.postRequest("/healthInfo/add", jsonBody, context, callback, needsToken = Constants.NEEDS_TOKEN, useJson = true)
    }

    // 添加或更新健康信息
    fun addOrUpdateHealthInfo(healthInfo: HealthInfo, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // 设置全局日期格式
            .registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
                src?.let {
                    JsonPrimitive(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(src))
                }
            })
            .create()
        val jsonBody = gson.toJson(healthInfo)

        OkhttpUtil.postRequest("/healthInfo/addOrUpdate", jsonBody, context, callback, needsToken = Constants.NEEDS_TOKEN, useJson = true)
    }

    // 通过特定的userId获取健康信息
    fun getHealthInfoByUserId(userId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("/healthInfo/${userId}", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }

    // 获取我的健康信息
    fun getHealthInfoOfMine(context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("/healthInfo/${AccountUtil(context).getUserId()}", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
}