package com.example.chms_android.api

import android.content.Context
import com.example.chms_android.dto.DoctorDTO
import com.example.chms_android.utils.OkhttpUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.sql.Timestamp
import java.text.SimpleDateFormat

object DoctorApi {

    // 获取所有医生
    fun getAllDoctors(context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("/doctor/getAll", context, callback, needsToken = false)
    }
    
    // 根据ID获取医生信息
    fun getDoctorById(doctorId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("/doctor/${doctorId}", context, callback, needsToken = false)
    }
    
    // 分页获取医生列表
    fun getDoctorsByPage(offset: Int, limit: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("/doctor/page?offset=${offset}&limit=${limit}", context, callback, needsToken = false)
    }
    
    // 根据用户ID获取医生信息
    fun getDoctorByUserId(userId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("/doctor/user/${userId}", context, callback, needsToken = true)
    }
    
    // 新增或更新医生信息
    fun saveOrUpdateDoctor(doctorDTO: DoctorDTO, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // 设置全局日期格式
            .registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
                src?.let {
                    JsonPrimitive(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(src))
                }
            })
            .create()
        val jsonBody = gson.toJson(doctorDTO)
        OkhttpUtil.postRequest("/doctor/saveOrUpdate", jsonBody, context, callback, needsToken = true, useJson = true)
    }
}