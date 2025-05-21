package com.example.chms_android.api

import android.content.Context
import android.util.Log
import com.example.chms_android.common.Constants
import com.example.chms_android.dto.DoctorAdviceDTO
import com.example.chms_android.utils.OkhttpUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.sql.Timestamp
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.text.SimpleDateFormat

object DoctorAdviceApi {
    private const val BASE_PATH = "/doctorAdvice"
    
    /**
     * 根据ID获取医生建议
     */
    fun getAdviceById(adviceId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "${BASE_PATH}/${adviceId}"
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 根据医生ID获取建议列表
     */
    fun getAdvicesByDoctorId(doctorId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "${BASE_PATH}/doctor/${doctorId}"
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 根据居民ID获取建议列表
     */
    fun getAdvicesByResidentId(residentId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "${BASE_PATH}/resident/${residentId}"
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 新增或更新医生建议
     */
    fun addOrUpdateAdvice(adviceDTO: DoctorAdviceDTO, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "${BASE_PATH}/addOrUpdate"
        
        // 创建支持日期格式的Gson实例，使用服务器期望的格式
        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
                src?.let {
                    JsonPrimitive(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(src))
                }
            })
            .create()
        
        val jsonBody = gson.toJson(adviceDTO)
        Log.d("DoctorAdviceApi", "Request body: $jsonBody")
        
        OkhttpUtil.postRequest(url, jsonBody, context, callback, needsToken = Constants.NEEDS_TOKEN, useJson = true)
    }
    
    /**
     * 更新医生建议状态
     */
    fun updateAdviceStatus(adviceId: Int, status: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "${BASE_PATH}/updateStatus?adviceId=${adviceId}&status=${status}"
        OkhttpUtil.postRequest(url, "", context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 分页查询医生建议
     */
    fun getAdvicesByPage(pageNum: Int, pageSize: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "${BASE_PATH}/page?pageNum=${pageNum}&pageSize=${pageSize}"
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 条件查询医生建议
     */
    fun searchAdvices(
        doctorId: Int? = null,
        residentId: Int? = null,
        adviceType: Int? = null,
        status: Int? = null,
        isImportant: Int? = null,
        pageNum: Int = 1,
        pageSize: Int = 10,
        context: Context,
        callback: OkhttpUtil.NetworkCallback
    ) {
        val params = mutableListOf<String>()
        
        doctorId?.let { params.add("doctorId=$it") }
        residentId?.let { params.add("residentId=$it") }
        adviceType?.let { params.add("adviceType=$it") }
        status?.let { params.add("status=$it") }
        isImportant?.let { params.add("isImportant=$it") }
        params.add("pageNum=$pageNum")
        params.add("pageSize=$pageSize")
        
        val queryString = params.joinToString("&")
        val url = "${BASE_PATH}/search?$queryString"
        
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
}