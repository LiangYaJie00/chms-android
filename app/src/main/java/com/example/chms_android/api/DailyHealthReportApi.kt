package com.example.chms_android.api

import android.content.Context
import com.example.chms_android.common.Constants
import com.example.chms_android.data.DailyHealthReport
import com.example.chms_android.utils.OkhttpUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.sql.Timestamp
import java.text.SimpleDateFormat

object DailyHealthReportApi {

    fun addDailyHealthReport(dailyHealthReport: DailyHealthReport, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // 设置全局日期格式
            .registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
                src?.let {
                    JsonPrimitive(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(src))
                }
            })
            .create()
        val jsonBody = gson.toJson(dailyHealthReport)

        OkhttpUtil.postRequest("/reports/addReport", jsonBody, context, callback, needsToken = Constants.NEEDS_TOKEN, useJson = true)
    }
    
    /**
     * 添加或更新健康日报
     * 
     * @param dailyHealthReport 健康日报对象
     * @param context 上下文
     * @param callback 网络回调
     */
    fun addOrUpdateDailyHealthReport(dailyHealthReport: DailyHealthReport, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // 设置全局日期格式
            .registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
                src?.let {
                    JsonPrimitive(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(src))
                }
            })
            .create()
        val jsonBody = gson.toJson(dailyHealthReport)

        OkhttpUtil.postRequest("/reports/addOrUpdateReport", jsonBody, context, callback, needsToken = Constants.NEEDS_TOKEN, useJson = true)
    }

    /**
     * 根据用户ID和报告日期获取健康报告
     * 
     * @param userId 用户ID
     * @param reportDate 报告日期，格式为 "yyyy-MM-dd"
     * @param context 上下文
     * @param callback 网络回调
     */
    fun getReportByUserIdAndDate(userId: Int, reportDate: String, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "/reports/getReportByUserIdAndDate?userId=$userId&reportDate=$reportDate"
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }

    /**
     * 获取某个用户的所有健康报告
     * 
     * @param userId 用户ID
     * @param context 上下文
     * @param callback 网络回调
     */
    fun getReportsByUserId(userId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "/reports/getReportsByUserId/$userId"
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }

    /**
     * 根据ID获取单个健康报告
     * 
     * @param reportId 报告ID
     * @param context 上下文
     * @param callback 网络回调
     */
    fun getReportById(reportId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "/reports/getReportById/$reportId"
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }

    /**
     * 检查用户当日是否已有健康报告
     * 
     * @param userId 用户ID
     * @param context 上下文
     * @param callback 网络回调
     */
    fun checkTodayReport(userId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "/reports/checkTodayReport?userId=$userId"
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
}
