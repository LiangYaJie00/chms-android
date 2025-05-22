package com.example.chms_android.api

import android.content.Context
import com.example.chms_android.common.Constants
import com.example.chms_android.dto.DoctorAvailableTimeRequestDTO
import com.example.chms_android.utils.OkhttpUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.sql.Timestamp
import java.text.SimpleDateFormat

object DoctorAvailableTimeApi {
    private const val BASE_PATH = "/doctorAvailableTime"
    
    /**
     * 添加医生可用时间段
     */
    fun addAvailableTimes(requestDTO: DoctorAvailableTimeRequestDTO, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
                src?.let {
                    JsonPrimitive(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(src))
                }
            })
            .create()
        
        val jsonBody = gson.toJson(requestDTO)
        OkhttpUtil.postRequest("$BASE_PATH/add", jsonBody, context, callback, needsToken = Constants.NEEDS_TOKEN, useJson = true)
    }
    
    /**
     * 删除医生可用时间段
     */
    fun deleteAvailableTime(timeId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.deleteRequest("$BASE_PATH/$timeId", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 删除医生某天的所有可用时间段
     */
    fun deleteAvailableTimesByDate(doctorId: Int, date: String, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.deleteRequest("$BASE_PATH/doctor/$doctorId/date/$date", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 根据ID查询医生可用时间段
     */
    fun getAvailableTimeById(timeId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("$BASE_PATH/$timeId", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 查询医生某天的所有可用时间段
     */
    fun getAvailableTimesByDate(doctorId: Int, date: String, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("$BASE_PATH/doctor/$doctorId/date/$date", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 查询医生某天的所有未被预约的时间段
     */
    fun getAvailableTimesByDateNotBooked(doctorId: Int, date: String, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("$BASE_PATH/doctor/$doctorId/date/$date/available", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 检查时间段是否可用
     */
    fun isTimeSlotAvailable(
        doctorId: Int, 
        date: String, 
        startTime: String, 
        endTime: String, 
        context: Context, 
        callback: OkhttpUtil.NetworkCallback
    ) {
        val url = "$BASE_PATH/check?doctorId=$doctorId&date=$date&startTime=$startTime&endTime=$endTime"
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
}