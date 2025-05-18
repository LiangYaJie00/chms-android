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

object ReportAnalysisApi {
    
    /**
     * 根据报告ID获取分析报告
     * 
     * @param reportId 报告ID
     * @param context 上下文
     * @param callback 网络回调
     */
    fun getReportAnalysisByReportId(reportId: Long, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "/analysis/getByReportId/$reportId"
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
    
    /**
     * 分析健康日报（直接发送日报数据进行分析）
     * 注意：此接口响应时间较长，约需10秒
     * 
     * @param dailyHealthReport 健康日报对象
     * @param context 上下文
     * @param callback 网络回调
     */
    fun analyzeDailyHealthReport(dailyHealthReport: DailyHealthReport, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // 设置全局日期格式
            .registerTypeAdapter(Timestamp::class.java, JsonSerializer<Timestamp> { src, _, _ ->
                src?.let {
                    JsonPrimitive(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(src))
                }
            })
            .create()
        val jsonBody = gson.toJson(dailyHealthReport)
        
        // 设置较长的超时时间，因为AI分析需要更多时间
        OkhttpUtil.postRequest(
            "/analysis/analyze", 
            jsonBody, 
            context, 
            callback, 
            needsToken = Constants.NEEDS_TOKEN, 
            useJson = true
        )
    }

    /**
     * 根据用户ID获取分析报告列表
     * 
     * @param userId 用户ID
     * @param context 上下文
     * @param callback 网络回调
     */
    fun getUserReportAnalysis(userId: Int, context: Context, callback: OkhttpUtil.NetworkCallback) {
        val url = "/analysis/getByUserId/$userId"
        OkhttpUtil.getRequest(url, context, callback, needsToken = Constants.NEEDS_TOKEN)
    }
}
