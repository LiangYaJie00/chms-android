package com.example.chms_android.api

import android.content.Context
import com.example.chms_android.common.Constants
import com.example.chms_android.dto.AppointmentRequestDTO
import com.example.chms_android.dto.AppointmentResponseDTO
import com.example.chms_android.utils.OkhttpUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.sql.Timestamp
import java.text.SimpleDateFormat

object AppointmentApi {
    private const val BASE_PATH = "/appointment"
    
    /**
     * 创建预约
     */
    fun createAppointment(requestDTO: AppointmentRequestDTO, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
                src?.let {
                    JsonPrimitive(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(src))
                }
            })
            .create()
        
        val jsonBody = gson.toJson(requestDTO)
        OkhttpUtil.postRequest("$BASE_PATH/create", jsonBody, context, callback, needsToken = Constants.NEEDS_TOKEN, useJson = true)
    }
    
    /**
     * 更新预约状态
     */
    fun updateAppointmentStatus(responseDTO: AppointmentResponseDTO, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
                src?.let {
                    JsonPrimitive(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(src))
                }
            })
            .create()
        
        val jsonBody = gson.toJson(responseDTO)
        OkhttpUtil.postRequest("$BASE_PATH/updateStatus", jsonBody, context, callback, needsToken = Constants.NEEDS_TOKEN, useJson = true)
    }
    
    /**
     * 根据ID查询预约
     */
    fun getAppointmentById(appointmentId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("$BASE_PATH/$appointmentId", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 查询患者的所有预约
     */
    fun getAppointmentsByPatientId(patientId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("$BASE_PATH/patient/$patientId", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 查询医生的所有预约
     */
    fun getAppointmentsByDoctorId(doctorId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("$BASE_PATH/doctor/$doctorId", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 查询特定日期的预约
     */
    fun getAppointmentsByDate(doctorId: Int, date: String, context: Context, callback: OkhttpUtil.NetworkCallback) {
        OkhttpUtil.getRequest("$BASE_PATH/doctor/$doctorId/date/$date", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 取消预约
     */
    fun cancelAppointment(appointmentId: Int, notes: String?, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = if (notes != null) {
            "$BASE_PATH/cancel/$appointmentId?notes=$notes"
        } else {
            "$BASE_PATH/cancel/$appointmentId"
        }
        
        OkhttpUtil.postRequest(url, "", context, callback, needsToken = Constants.NEEDS_TOKEN, useJson = true)
    }
}